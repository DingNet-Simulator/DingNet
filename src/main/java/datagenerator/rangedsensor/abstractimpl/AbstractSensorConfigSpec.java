package datagenerator.rangedsensor.abstractimpl;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeSensor;
import datagenerator.rangedsensor.api.TimeUnit;

import java.util.List;

abstract public class AbstractSensorConfigSpec<RS extends RangeSensor, C extends Cell> {

    public final ConfigSpec SPEC = new ConfigSpec("sensor");
    public final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};
    public final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};
    public RequiredItem<RS> defaultLevel = getDefaultLevelItem();
    public final RequiredItem<TimeUnit> timeUnit= new RequiredItem<>(SPEC, "timeUnit") {};
    public RequiredItem<List<C>> cells = getCellsItem();

    abstract protected RequiredItem<RS> getDefaultLevelItem();
    abstract protected RequiredItem<List<C>> getCellsItem();
}
