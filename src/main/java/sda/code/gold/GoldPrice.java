
package sda.code.gold;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GoldPrice {

    @SerializedName("data")
    @Expose
    public String data;
    @SerializedName("cena")
    @Expose
    public Double cena;

}
