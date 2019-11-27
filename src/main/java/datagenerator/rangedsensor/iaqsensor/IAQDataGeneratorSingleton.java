package datagenerator.rangedsensor.iaqsensor;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;
import datagenerator.rangedsensor.api.Cell;

import java.util.stream.Collectors;


public class IAQDataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/IAQSensorConfig.toml";

    private IAQDataGeneratorSingleton() {
        super();
        Config config = new BaseConfig();
        config.addSpec(IAQSensorConfigSpec.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(configFile));
        row = config.get(IAQSensorConfigSpec.row);
        columns = config.get(IAQSensorConfigSpec.columns);
        defaultLevel = config.get(IAQSensorConfigSpec.defaultLevel);
        timeUnit = config.get(IAQSensorConfigSpec.timeUnit);
        map = config.get(IAQSensorConfigSpec.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new IAQDataGeneratorSingleton();
        }
        return instance;
    }
}
