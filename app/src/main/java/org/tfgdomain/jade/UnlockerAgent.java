package unboundId;
/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase UnlockerAgent.java (generada en Eclipse para su ejecución en plataforma JADE, no forma
 * parte del proyecto Android Studio, pero se incluye aquí a efectos de revisión de código.
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UnlockerAgent extends Agent{
    /**
     *
     */
    private static final long serialVersionUID = 7839279863698345689L;

    private Codec codec = new SLCodec();
    private Ontology ontology = LdapOntology.getInstance();
    private String us = null;
    private String pwd = null;


    protected void setup() {

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);




        getAID().addAddresses("http://TFGDC.tfgdomain.org:7778/acc");

        while (us == null){
            us = askForUser();
        }
        while (pwd == null){
            pwd = askForPassword();
        }


        addBehaviour(new CyclicBehaviour(this) {


            private static final long serialVersionUID = 3296830928561423659L;

            @Override
            public void action() {

                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchLanguage(codec.getName()),
                        MessageTemplate.MatchOntology(ontology.getName()));
                ACLMessage aclMsg = blockingReceive(mt);

                if(aclMsg != null) {
                    if(aclMsg.getPerformative() == ACLMessage.REQUEST) {


                        ContentElement ce;


                        try {
                            ce = getContentManager().extractContent(aclMsg);

                            if (ce instanceof UnlockRequest) {
                                ACLMessage reply = aclMsg.createReply();
                                reply.setLanguage(codec.getName());
                                reply.setOntology(ontology.getName());
                                reply.setPerformative(ACLMessage.INFORM);

                                Unlock unlock = new Unlock();
                                unlock.setAccount(((UnlockRequest) ce).getAccount());

                                unlock.setResultCode(processUnlock(unlock));
                                Action unlockAc = new Action(getAID(), unlock);
                                getContentManager().fillContent(reply, unlockAc);
                                send(reply);

                            }
                        } catch (CodecException | OntologyException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    } else {
                        block();
                    }
                }

            }

        } );


    }

    private String askForUser() {
        JTextField jtf = new JTextField();
        int answer = JOptionPane.showConfirmDialog(null, jtf, "Introduzca usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer == JOptionPane.OK_OPTION){
            return jtf.getText();
        } else {return null;}
    }

    private String askForPassword() {
        JPasswordField jpf = new JPasswordField();
        int answer = JOptionPane.showConfirmDialog(null, jpf, "Introduzca contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer == JOptionPane.OK_OPTION){

            return new String(jpf.getPassword());
        } else {return null;}
    }

    private int processUnlock(Unlock unlock) {
        int result = ResultCode.OPERATIONS_ERROR_INT_VALUE;
        LDAPConnection c = null;
        LDAPResult modifyResult = null;
        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        try {
            SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory("TLSv1.2");
            c = new LDAPConnection(socketFactory);

            //System.out.println(pwd);
            System.out.println(us+"@"+unlock.getAccount().getDomain());
            try {
                c.connect(unlock.getAccount().getDomain(), 636);
                BindRequest bindRequest = new SimpleBindRequest(us+"@"+unlock.getAccount().getDomain(),pwd);
                BindResult bindResult = c.bind(bindRequest);
                if (bindResult.getResultCode().equals(ResultCode.SUCCESS)){
                    System.out.println("Conexion OK.");
                    //
                    Filter filter = Filter.createEqualityFilter("sAMAccountName",unlock.getAccount().getUser());
                    SearchResult searchResult = c.search(getDN(unlock.getAccount().getDomain()), SearchScope.SUB, filter,"distinguishedName");
                    SearchResultEntry entry = searchResult.getSearchEntries().get(0);
                    String dn = entry.getAttributeValue("distinguishedName");
                    //
                    Modification mod = new Modification(ModificationType.REPLACE,"lockoutTime","0");
                    ModifyRequest modifyRequest = new ModifyRequest(dn,mod);
                    modifyResult = c.modify(modifyRequest);
                    if (modifyResult.getResultCode().equals(ResultCode.SUCCESS)){
                        result = ResultCode.SUCCESS_INT_VALUE;
                    }

                }
            } catch (LDAPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (GeneralSecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            c.close();
        }

        return result;




        /*
         * Desbloquear cuenta de usuario con usuario de servicio.
         * La idea será crear métodos para tareas concretas sobre las cuentas, por ejemplo desbloquearUsuario(usuario)
         */
		/*
		Modification mod = new Modification(ModificationType.REPLACE,"lockoutTime","0");

		ModifyRequest modifyRequest = new ModifyRequest("CN=Usuario 1,OU=Usuarios,OU=Test,DC=tfgdomain,DC=org",mod);

		c.modify(modifyRequest);*/
    }
    private String getDN(String host){
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
