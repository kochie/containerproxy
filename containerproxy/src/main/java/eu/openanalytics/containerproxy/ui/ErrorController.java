package eu.openanalytics.containerproxy.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.NestedServletException;

import eu.openanalytics.containerproxy.api.BaseController;

@Controller
@RequestMapping("/error")
public class ErrorController extends BaseController implements org.springframework.boot.web.servlet.error.ErrorController {
	
	@RequestMapping(produces = "text/html")
	public String handleError(ModelMap map, HttpServletRequest request, HttpServletResponse response) {
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
		String[] msg = createMsgStack(exception);
		
		map.put("message", msg[0]);
		map.put("stackTrace", msg[1]);
		map.put("status", response.getStatus());
		
		return "error";
	}
	
	@RequestMapping(consumes = "application/json", produces = "application/json")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request, HttpServletResponse response) {
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
		String[] msg = createMsgStack(exception);
		
		Map<String, Object> map = new HashMap<>();
		map.put("message", msg[0]);
		map.put("stackTrace", msg[1]);
		
		return new ResponseEntity<>(map, HttpStatus.valueOf(response.getStatus()));
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

	private String[] createMsgStack(Throwable exception) {
		String message = "";
		String stackTrace = "";
		
		if (exception instanceof NestedServletException && exception.getCause() instanceof Exception) {
			exception = (Exception) exception.getCause();
		}
		if (exception != null) {
			if (exception.getMessage() != null) message = exception.getMessage();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try (PrintWriter writer = new PrintWriter(bos)) {
				exception.printStackTrace(writer);
			}
			stackTrace = bos.toString();
			stackTrace = stackTrace.replace(System.getProperty("line.separator"), "<br/>");
		}
		
		if (message == null || message.isEmpty()) message = "An unexpected server error occurred";
		
		return new String[] { message, stackTrace };
	}
}