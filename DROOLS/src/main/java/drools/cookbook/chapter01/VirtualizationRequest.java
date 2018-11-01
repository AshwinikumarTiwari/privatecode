package drools.somedomain.box01;

import java.io.Serializable;

/**
 * 
 * @author Lucas Amador
 * 
 */
public class VirtualizationRequest implements Serializable {

    private String serverName;
    private Virtualization virtualization;
    private boolean successful;

    public VirtualizationRequest(Virtualization virtualization) {
        this.serverName = virtualization.getServerName();
        this.virtualization = virtualization;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setVirtualization(Virtualization virtualization) {
        this.virtualization = virtualization;
    }

    public Virtualization getVirtualization() {
        return virtualization;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }

}
