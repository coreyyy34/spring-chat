package nz.coreyh.springchat.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private val log = LoggerFactory.getLogger(CorrelationIdFilter::class.java)

@Component
class CorrelationIdFilter : OncePerRequestFilter() {

    companion object {
        const val MDC_KEY: String = "correlationId"
        const val HEADER_NAME = "X-Correlation-ID"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val correlationId = UUID.randomUUID().toString()
        MDC.put(MDC_KEY, correlationId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }
}