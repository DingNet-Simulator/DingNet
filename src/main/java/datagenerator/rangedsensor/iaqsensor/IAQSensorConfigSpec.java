package datagenerator.rangedsensor.iaqsensor;

import com.uchuhimo.konf.RequiredItem;
import datagenerator.rangedsensor.abstractimpl.AbstractSensorConfigSpec;

import java.util.List;

public class IAQSensorConfigSpec extends AbstractSensorConfigSpec<AirQualityLevel, IAQCell> {

    @Override
    protected RequiredItem<AirQualityLevel> getDefaultLevelItem() {
        return new RequiredItem<>(SPEC, "defaultLevel") {};
    }

    @Override
    protected RequiredItem<List<IAQCell>> getCellsItem() {
        return new RequiredItem<>(SPEC, "cell") {};
    }
}
