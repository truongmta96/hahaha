package vn.stationery.controller;
import org.springframework.web.bind.annotation.*; import org.springframework.http.*;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler({IllegalArgumentException.class,javax.validation.ConstraintViolationException.class}) public ResponseEntity<?> bad(Exception e) { return ResponseEntity.badRequest().body(java.util.Map.of("message",e.getMessage()==null?"Dữ liệu không hợp lệ":e.getMessage())); }
 @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) public ResponseEntity<?> validation(org.springframework.web.bind.MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(java.util.Map.of("message","Dữ liệu không hợp lệ","fields",e.getBindingResult().getFieldErrors().stream().map(x -> x.getField()+": "+x.getDefaultMessage()).collect(java.util.stream.Collectors.toList()))); }
 @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT) public Object conflict() { return java.util.Map.of("message","Dữ liệu trùng, vượt giới hạn hoặc tham chiếu không hợp lệ"); }
 @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class) @ResponseStatus(HttpStatus.CONFLICT) public Object concurrency() { return java.util.Map.of("message","Xung đột giao dịch; vui lòng thử lại"); }
}
