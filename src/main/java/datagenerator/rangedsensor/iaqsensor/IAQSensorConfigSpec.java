package datagenerator.rangedsensor.iaqsensor;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.api.TimeUnit;

import java.util.List;

public class IAQSensorConfigSpec {

    public static final ConfigSpec SPEC = new ConfigSpec("IAQSensor");

    public static final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};

    public static final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};

    public static final RequiredItem<AirQualityLevel> defaultLevel = new RequiredItem<>(SPEC, "defaultLevel") {};

    public static final RequiredItem<TimeUnit> timeUnit= new RequiredItem<>(SPEC, "timeUnit") {};

    public static final RequiredItem<List<IAQCell>> cells = new RequiredItem<>(SPEC, "cell") {};


}
