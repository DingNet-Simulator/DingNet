package datagenerator.rangedsensor.abstractimpl;

import com.uchuhimo.konf.ConfigSpec;
import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeValue;
import datagenerator.rangedsensor.api.TimeUnit;

import java.util.List;

abstract public class AbstractSensorConfigSpec<RV extends RangeValue, C extends Cell> {

    public final ConfigSpec SPEC = new ConfigSpec("sensor");
    public final RequiredItem<Integer> row = new RequiredItem<>(SPEC, "row") {};
    public final RequiredItem<Integer> columns = new RequiredItem<>(SPEC, "columns") {};
    public final RequiredItem<RV> defaultLevel = getDefaultLevelItem();
    public final RequiredItem<TimeUnit> timeUnit = new RequiredItem<>(SPEC, "timeUnit") {};
    public final RequiredItem<List<C>> cells = getCellsItem();

    abstract protected RequiredItem<RV> getDefaultLevelItem();
    abstract protected RequiredItem<List<C>> getCellsItem();
}
