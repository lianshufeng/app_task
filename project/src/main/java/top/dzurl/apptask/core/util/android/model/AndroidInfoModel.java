package top.dzurl.apptask.core.util.android.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AndroidInfoModel {
    private String versionCode;
    private String versionName;
    private String packageName;

}
