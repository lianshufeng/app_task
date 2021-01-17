package top.dzurl.apptask.core.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import top.dzurl.apptask.core.result.ResultException;
import top.dzurl.apptask.core.result.ResultState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class InvokerExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        e.printStackTrace();
        log.error("exception : {}", e);
        ModelAndView mv = new ModelAndView();
        mv.addObject("state", ResultState.Exception);
        mv.setView(new MappingJackson2JsonView());
        mv.addObject("exception", ResultException.build(e));
        return mv;
    }

}
