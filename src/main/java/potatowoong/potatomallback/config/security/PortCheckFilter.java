package potatowoong.potatomallback.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PortCheckFilter extends OncePerRequestFilter {

    @Value("${server.port}")
    private String apiAllowedPort;

    @Value("${server.docs-port}")
    private String docsAllowedPort;

    private final AntPathRequestMatcher apiRequestMatcher;

    private final AntPathRequestMatcher docsRequestMatcher;

    public PortCheckFilter() {
        this.apiRequestMatcher = new AntPathRequestMatcher("/api/**");
        this.docsRequestMatcher = new AntPathRequestMatcher("/docs/**");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // API 요청 제한
        if (apiRequestMatcher.matches(request) && request.getLocalPort() != Integer.parseInt(apiAllowedPort)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        // Docs 요청 제한
        if (docsRequestMatcher.matches(request) && request.getLocalPort() != Integer.parseInt(docsAllowedPort)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        filterChain.doFilter(request, response);
    }
}
