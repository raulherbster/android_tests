package org.mpi_sws.testdialog;

/**
 * Created by herbster on 29/12/15.
 */
public class SecureChannelContact {

    private String contactAddress;
    private String contactName;

    public SecureChannelContact(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

}
