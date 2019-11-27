package datagenerator.rangedsensor.no2sensor;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;
import datagenerator.rangedsensor.api.Cell;

import java.util.stream.Collectors;

public class NO2DataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/no2SensorConfig.toml";

    private NO2DataGeneratorSingleton() {
        super();
        Config config = new BaseConfig();
        config.addSpec(NO2SensorConfigSpec.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(configFile));
        row = config.get(NO2SensorConfigSpec.row);
        columns = config.get(NO2SensorConfigSpec.columns);
        defaultLevel = config.get(NO2SensorConfigSpec.defaultLevel);
        timeUnit = config.get(NO2SensorConfigSpec.timeUnit);
        map = config.get(NO2SensorConfigSpec.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new NO2DataGeneratorSingleton();
        }
        return instance;
    }

    @Override
    public int getAmountOfData() {
        return 2;
    }
}
