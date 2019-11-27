package datagenerator.rangedsensor.pm10sensor;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.api.TimeUnit;

import java.util.List;

public class PM10SensorConfigSpec {

    public static final ConfigSpec SPEC = new ConfigSpec("PM10Sensor");

    public static final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};

    public static final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};

    public static final RequiredItem<PM10Level> defaultLevel = new RequiredItem<>(SPEC, "defaultLevel") {};

    public static final RequiredItem<TimeUnit> timeUnit= new RequiredItem<>(SPEC, "timeUnit") {};

    public static final RequiredItem<List<PM10Cell>> cells = new RequiredItem<>(SPEC, "cell") {};
}
