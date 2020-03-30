package datagenerator.rangedsensor.pm10sensor;

import datagenerator.rangedsensor.abstractimpl.RangeDataGenerator;

public class PM10DataGenerator extends RangeDataGenerator {

    private static final String configFile = "/sensorsConfigurations/pm10SensorConfig.toml";

    public PM10DataGenerator() {
        super(new PM10SensorConfigSpec());
    }

    @Override
    protected String getDefaultConfigFilePath() {
        return configFile;
    }
}
