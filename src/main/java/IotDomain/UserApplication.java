package IotDomain;

import be.kuleuven.cs.som.annotate.Basic;

/**
 * A class representing a user application.
 */
public abstract class UserApplication {
    /**
     * A long representing a unique application identifier.
     */
    private final Long appEUI;

    /**
     * Constructs a user application with a given EUI.
     * @param appEUI
     */
    public  UserApplication(Long appEUI){
        this.appEUI = appEUI;
    }

    /**
     * Returns the application EUI.
     * @return The application EUI.
     */
    @Basic
    public Long getAppEUI() {
        return appEUI;
    }
}
