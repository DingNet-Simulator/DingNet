package datagenerator.rangedsensor.no2sensor;

import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.abstractimpl.AbstractSensorConfigSpec;

import java.util.List;

public class NO2SensorConfigSpec extends AbstractSensorConfigSpec<NO2Level, NO2Cell> {

    @Override
    protected RequiredItem<NO2Level> getDefaultLevelItem() {
        return new RequiredItem<>(SPEC, "defaultLevel") {};
    }

    @Override
    protected RequiredItem<List<NO2Cell>> getCellsItem() {
        return new RequiredItem<>(SPEC, "cell") {};
    }
}
