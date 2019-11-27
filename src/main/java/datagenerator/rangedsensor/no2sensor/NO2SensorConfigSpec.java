package datagenerator.rangedsensor.no2sensor;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.api.TimeUnit;

import java.util.List;

public class NO2SensorConfigSpec {

    public static final ConfigSpec SPEC = new ConfigSpec("PM10Sensor");

    public static final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};

    public static final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};

    public static final RequiredItem<NO2Level> defaultLevel = new RequiredItem<>(SPEC, "defaultLevel") {};

    public static final RequiredItem<TimeUnit> timeUnit= new RequiredItem<>(SPEC, "timeUnit") {};

    public static final RequiredItem<List<NO2Cell>> cells = new RequiredItem<>(SPEC, "cell") {};
}
