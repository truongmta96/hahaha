package vn.stationery;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.stationery.service.*;
import vn.stationery.dto.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={"app.bootstrap-password=TestAdminPassword123!","spring.cache.type=simple"})
@AutoConfigureMockMvc @Testcontainers
class WorkflowTest {
 @Container static final MySQLContainer<?> MYSQL=new MySQLContainer<>("mysql:8.0.41");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",MYSQL::getJdbcUrl); r.add("spring.datasource.username",MYSQL::getUsername); r.add("spring.datasource.password",MYSQL::getPassword);
 }
 @Autowired ReceiptService receipts; @Autowired OrderService orders; @Autowired JdbcTemplate jdbc; @Autowired MockMvc mvc;
 Long product,customer;
 void actor(String name,String...permissions) {
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(name,"unused",Arrays.stream(permissions).map(SimpleGrantedAuthority::new).collect(java.util.stream.Collectors.toList())));
 }
 @BeforeEach void setup() {
  actor("admin");
  jdbc.update("INSERT IGNORE INTO user_accounts(username,password_hash,enabled) VALUES ('manager','unused',1),('director','unused',1)");
  jdbc.update("UPDATE user_accounts SET password_hash=(SELECT h FROM (SELECT password_hash h FROM user_accounts WHERE username='admin') a) WHERE username='manager'");
  jdbc.update("INSERT IGNORE INTO user_roles SELECT u.id,r.id FROM user_accounts u CROSS JOIN roles r WHERE u.username='manager' AND r.code='MANAGER'");
  String code=UUID.randomUUID().toString();
  jdbc.update("INSERT INTO products(sku,name,category,unit,price,active) VALUES (?, 'Bút','Bút viết','Cái',10000,1)",code);
  product=jdbc.queryForObject("SELECT id FROM products WHERE sku=?",Long.class,code);
  jdbc.update("INSERT INTO customers(code,name) VALUES (?, 'Khách kiểm thử')",code);
  customer=jdbc.queryForObject("SELECT id FROM customers WHERE code=?",Long.class,code);
 }
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 DocumentRequest request(long quantity){
  DocumentRequest r=new DocumentRequest();r.warehouseId=1L;r.customerId=customer;r.supplier="Nhà cung cấp";
  DocumentRequest.Line l=new DocumentRequest.Line();l.productId=product;l.quantity=quantity;l.unitPrice=BigDecimal.ONE;r.lines=List.of(l);return r;
 }
 DecisionRequest approve(){DecisionRequest d=new DecisionRequest();d.decision="APPROVE";return d;}
 long stock(){return Optional.ofNullable(jdbc.queryForObject("SELECT SUM(quantity) FROM inventory_balances WHERE product_id=?",Long.class,product)).orElse(0L);}
 void receive(long quantity){
  actor("admin");Long id=receipts.create(request(quantity)).getId();
  actor("manager","RECEIPT_APPROVE_1");receipts.decide(id,approve());
  assertEquals(0,stock());
  actor("director","RECEIPT_APPROVE_2");receipts.decide(id,approve());actor("admin");
 }
 @Test void twoApprovalsAndRepeatedFulfillmentMoveStockExactlyOnce(){
  receive(10);assertEquals(10,stock());
  Long id=orders.create(request(3)).getId();assertEquals(10,stock());
  assertEquals(new BigDecimal("30000.00"),orders.fulfill(id).getTotal().setScale(2));
  orders.fulfill(id);assertEquals(7,stock());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM stock_movements WHERE kind='SALE' AND document_id=?",Integer.class,id));
 }
 @Test void insufficientStockRollsBackOrderAndMovement(){
  receive(2);Long id=orders.create(request(3)).getId();
  assertThrows(IllegalArgumentException.class,()->orders.fulfill(id));assertEquals(2,stock());
  assertEquals("PLACED",jdbc.queryForObject("SELECT status FROM sales_orders WHERE id=?",String.class,id));
  assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM stock_movements WHERE kind='SALE' AND document_id=?",Integer.class,id));
 }
 @Test void creatorAndFirstApproverCannotApproveAgain(){
  Long id=receipts.create(request(10)).getId();
  actor("admin","RECEIPT_APPROVE_1");assertThrows(IllegalArgumentException.class,()->receipts.decide(id,approve()));
  actor("manager","RECEIPT_APPROVE_1","RECEIPT_APPROVE_2");receipts.decide(id,approve());
  assertThrows(IllegalArgumentException.class,()->receipts.decide(id,approve()));assertEquals(0,stock());
 }
 @Test void readEndpointRequiresAuthenticationAndPermission() throws Exception {
  SecurityContextHolder.clearContext();mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
  String manager="Basic "+Base64.getEncoder().encodeToString("manager:TestAdminPassword123!".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  String admin="Basic "+Base64.getEncoder().encodeToString("admin:TestAdminPassword123!".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  mvc.perform(get("/api/products").header("Authorization",manager)).andExpect(status().isForbidden());
  mvc.perform(get("/api/products").header("Authorization",admin)).andExpect(status().isOk());
 }
 @Test void concurrentSalesCannotOversell() throws Exception {
  receive(5);Long a=orders.create(request(4)).getId(),b=orders.create(request(4)).getId();
  java.util.concurrent.ExecutorService pool=java.util.concurrent.Executors.newFixedThreadPool(2);
  java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
  java.util.concurrent.Callable<Boolean> first=()->sell(start,a),second=()->sell(start,b);
  try {
   java.util.concurrent.Future<Boolean> x=pool.submit(first),y=pool.submit(second);start.countDown();
   int successes=(x.get(20,java.util.concurrent.TimeUnit.SECONDS)?1:0)+(y.get(20,java.util.concurrent.TimeUnit.SECONDS)?1:0);
   assertEquals(1,successes);assertEquals(1,stock());
  }finally{pool.shutdownNow();}
 }
 boolean sell(java.util.concurrent.CountDownLatch start,Long id)throws Exception{
  actor("admin");try{start.await();orders.fulfill(id);return true;}catch(IllegalArgumentException e){return false;}finally{SecurityContextHolder.clearContext();}
 }
}
