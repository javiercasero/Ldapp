package org.tfgdomain.ldapp;

public class ListElement {
    private int idIcon;
    private String textPrincipal;
    private String textSecondary;
    private String userDN;
    private boolean checked;
    private int accountControl;

    public ListElement (int idIcon, String textPrincipal, String textSecondary){
        this.idIcon = idIcon;
        this.textPrincipal = textPrincipal;
        this.textSecondary = textSecondary;
        checked = false;
    }

    public int getIdIcon() {
        if (idIcon == 0) {
            return R.drawable.ic_person_black_24px;
        } else if (idIcon == 1) {
            return R.drawable.ic_supervisor_account_black_24px;
        }
        return idIcon;
    }

    public String getTextPrincipal() {
        return textPrincipal;
    }

    public String getTextSecondary() {
        return textSecondary;
    }

    public Integer getTypeOfUser() { return idIcon; }

    public void setChecked(boolean check){
        checked = check;
    }
    public boolean getChecked(){
        return checked;
    }

    public void setUserDN(String dn){
        userDN = dn;
    }
    public String getUserDN(){
        return userDN;
    }

    public void setAccountControl(int aC) {
        accountControl = aC;
    }
    public int getAccountControl(){
        return accountControl;
    }
}
