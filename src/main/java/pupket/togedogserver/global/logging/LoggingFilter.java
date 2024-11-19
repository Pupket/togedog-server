package pupket.togedogserver.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LoggingFilter extends OncePerRequestFilter {
    // 로그 파일이 저장될 경로를 상수로 정의
    private static final String LOG_FILE_PATH = "logs/access.log";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 요청 정보를 포맷에 맞춰 로그 메시지 생성
        // [HTTP메서드] URI?쿼리스트링 형식으로 기록
        String logMessage = String.format("[%s] %s %s", 
            request.getMethod(), 
            request.getRequestURI(),
            request.getQueryString() != null ? "?" + request.getQueryString() : ""
        );
        
        // 로그 파일에 메시지 작성
        // try-with-resources를 사용하여 자원을 자동으로 닫음
        try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true);  // true: append 모드로 파일 열기
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logMessage);
            bw.newLine();  // 새 줄 추가
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}