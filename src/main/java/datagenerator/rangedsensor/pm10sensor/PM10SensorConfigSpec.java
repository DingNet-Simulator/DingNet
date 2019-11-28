package datagenerator.rangedsensor.pm10sensor;

import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.abstractimpl.AbstractSensorConfigSpec;

import java.util.List;

public class PM10SensorConfigSpec extends AbstractSensorConfigSpec<PM10Level, PM10Cell> {

    @Override
    protected RequiredItem<PM10Level> getDefaultLevelItem() {
        return new RequiredItem<>(SPEC, "defaultLevel") {};
    }

    @Override
    protected RequiredItem<List<PM10Cell>> getCellsItem() {
        return new RequiredItem<>(SPEC, "cell") {};
    }
}
