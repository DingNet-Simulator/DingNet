package datagenerator.rangedsensor.pm10sensor;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;
import datagenerator.rangedsensor.api.Cell;

import java.util.stream.Collectors;

public class PM10DataGeneratorSingleton extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/pm10SensorConfig.toml";

    private PM10DataGeneratorSingleton() {
        super();
        Config config = new BaseConfig();
        config.addSpec(PM10SensorConfigSpec.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(configFile));
        row = config.get(PM10SensorConfigSpec.row);
        columns = config.get(PM10SensorConfigSpec.columns);
        defaultLevel = config.get(PM10SensorConfigSpec.defaultLevel);
        timeUnit = config.get(PM10SensorConfigSpec.timeUnit);
        map = config.get(PM10SensorConfigSpec.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
    }

    public static SensorDataGenerator getInstance() {
        if (instance == null) {
            instance = new PM10DataGeneratorSingleton();
        }
        return instance;
    }


}
