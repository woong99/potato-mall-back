package potatowoong.potatomallback.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientUtils {

    /**
     * 클라이언트 IP 조회
     *
     * @param request HttpServletRequest
     * @return 클라이언트 IP
     */
    public static String getRemoteIP(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");

        //proxy 환경일 경우
        if (!StringUtils.hasText(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        //웹로직 서버일 경우
        if (!StringUtils.hasText(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    /**
     * 클라이언트 IP 조회
     *
     * @return 클라이언트 IP
     */
    public static String getRemoteIP() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return getRemoteIP(request);
    }
}
