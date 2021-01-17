package top.dzurl.apptask.core.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.dzurl.apptask.core.conf.MapConf;
import top.dzurl.apptask.core.util.HttpClient;
import top.dzurl.apptask.core.util.JsonUtil;
import top.dzurl.apptask.core.util.LngLonUtil;

import java.util.Map;

@Component
public class MapHelper {

    @Autowired
    private MapConf mapConf;

    //访问次数
    private int accessCount = 0;


    /**
     * 查询地址
     *
     * @param address
     */
    @SneakyThrows
    public Location query(String address) {
        String url = String.format("http://api.map.baidu.com/geocoding/v3/?address=%s&output=json&ak=%s", address, getAk());
        Map<String, String> ret = JsonUtil.toObject(new String(new HttpClient().get(url)), Map.class);
        Object result = ret.get("result");
        if (result == null) {
            return null;
        }
        Object location = ((Map) result).get("location");
        if (location == null) {
            return null;
        }

        Location model = new Location();
        model.setLng(String.valueOf(((Map) location).get("lng")));
        model.setLat(String.valueOf(((Map) location).get("lat")));

        //进行坐标转换
        toWGS84(model);

        return model;
    }


    /**
     * 构建web应用的URL
     *
     * @param location
     * @return
     */
    public String makeWebUrl(Location location) {
        return String.format("http://api.map.baidu.com/marker?location=%s,%s&output=html", location.getLat(), location.getLng());
    }


    /**
     * 取出百度Api的ak
     */
    private synchronized String getAk() {
        String[] aks = mapConf.getAk();
        return aks[Math.abs(accessCount++) % aks.length];
    }


    @SneakyThrows
    private void toWGS84(Location location) {
        double[] newLocation = LngLonUtil.bd09_To_gps84(Double.valueOf(location.getLat()), Double.valueOf(location.getLng()));
        location.setLat(String.valueOf(newLocation[0]));
        location.setLng(String.valueOf(newLocation[1]));
    }


    /**
     * 坐标
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Location {
        //经度
        private String lng;
        //纬度
        private String lat;
    }

}
