package org.tfgdomain.ldapp;

public class BaseDN {
    public String getDN(String host){
        String dominioDN = "";
        String[] dominioDNArray = host.split("\\.");

        for (int i=0;i<dominioDNArray.length;i++){
            dominioDN=dominioDN.concat("DC=").concat(dominioDNArray[i]);
            if (i != dominioDNArray.length-1){
                dominioDN=dominioDN.concat(",");
            }
        }
        return dominioDN;
    }

}