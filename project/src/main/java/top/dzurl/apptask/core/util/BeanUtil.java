package top.dzurl.apptask.core.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.*;
import java.util.stream.Collectors;

public class BeanUtil {

    /**
     * 获取bean对象中为null的属性名
     *
     * @param source
     * @return
     */
    public static Set<String> getNullPropertyNames(Object source) {
        Set<String> ret = new HashSet<>();
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors()).filter((pd) -> {
            return src.getPropertyValue(pd.getName()) == null;
        }).map((it) -> {
            return it.getName();
        }).collect(Collectors.toSet());
    }


    /**
     * Bean转到map
     *
     * @param source
     * @return
     */
    public static Map<String, Object> toMap(Object source) {
        Map<String, Object> ret = new HashMap<>();
        final BeanWrapper src = new BeanWrapperImpl(source);
        Arrays.stream(src.getPropertyDescriptors()).forEach((it) -> {
            ret.put(it.getName(), src.getPropertyValue(it.getName()));
        });
        return ret;

    }


    /**
     * 获取有value值的key
     *
     * @param source
     * @return
     */
    public static Set<String> getHasPropertyNames(Object source) {
        Set<String> ret = new HashSet<>();
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors()).filter((pd) -> {
            return src.getPropertyValue(pd.getName()) != null;
        }).map((it) -> {
            return it.getName();
        }).collect(Collectors.toSet());
    }


    /**
     * 获取对象的所有属性名
     *
     * @param source
     * @return
     */
    public static Set<String> getPropertyNames(Object source) {
        Set<String> ret = new HashSet<>();
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors()).map((it) -> {
            return it.getName();
        }).collect(Collectors.toSet());
    }
}
