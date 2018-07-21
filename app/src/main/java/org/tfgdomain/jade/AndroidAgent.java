package org.tfgdomain.jade;

/**
 * TFG "App para gestión móvil de cuentas LDAP – Active Directory" en la Universidad Internacional de la Rioja
 * Descripción de la clase AndroidAgent.java
 * @author Javier Casero Sáenz de Jubera
 * @version 2.0, 2018/07/21
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.unboundid.ldap.sdk.ResultCode;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

public class AndroidAgent extends GatewayAgent {
    private static final long serialVersionUID = 1594371294421614291L;
    private Context context;
    private final Codec codec = new SLCodec();
    private final Ontology ontology = LdapOntology.getInstance();
    private ACLMessage jadeMsg;
    private String domain, user;

    protected void setup(){
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        getContentManager().setValidationMode(false);

        //registerLangAndOnto();
        domain = getProperty("domain", "0");
        user = getProperty("user", "0");

        Log.i("AndroidAgent", "setup");


            addBehaviour(new OneShotBehaviour(this) {
                @Override
                public void action() {
                    InetAddress address;

                    try {
                        address = InetAddress.getByName(domain);
                        System.out.println(address.getHostAddress());

                        AID remoteAID = new AID("ua@"+address.getHostAddress()+":1099/JADE",AID.ISGUID);
                        remoteAID.addAddresses("http://"+domain+":7778/acc");

                        jadeMsg = new ACLMessage(ACLMessage.REQUEST);
                        jadeMsg.addReceiver(remoteAID);

                        UnlockRequest unlockRequest = new UnlockRequest();
                        Account account = new Account();
                        account.setUser(user);
                        account.setDomain(domain);
                        unlockRequest.setAccount(account);
                        jadeMsg.setLanguage(codec.getName());
                        jadeMsg.setOntology(ontology.getName());

                        getContentManager().fillContent(jadeMsg, unlockRequest);
                        send(jadeMsg);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                }
            });

            addBehaviour(new CyclicBehaviour(this) {

                @Override
                public void action() {
                    MessageTemplate mt = MessageTemplate.and(
                            MessageTemplate.MatchLanguage(codec.getName()),
                            MessageTemplate.MatchOntology(ontology.getName()));

                    ACLMessage aclMsg = blockingReceive(mt);
                    if(aclMsg != null) {
                        if(aclMsg.getPerformative() == ACLMessage.INFORM) {

                            try {
                                ContentElement ce = getContentManager().extractContent(aclMsg);
                                Concept unlockAc = ((Action)ce).getAction();
                                if (unlockAc instanceof Unlock) {
                                    Log.i("AndroidAgent", String.valueOf(((Unlock) unlockAc).getResultCode()));
                                    if (((Unlock) unlockAc).getResultCode() == ResultCode.SUCCESS_INT_VALUE){
                                        //Acciones si el desbloqueo es correcto
                                        Log.i("AndroidAgent", "desbloqueo correcto");
                                        Intent broadcast = new Intent();
                                        broadcast.setAction("unlocked");
                                        context.sendBroadcast(broadcast);
                                    }

                                    doDelete();

                                }
                            } catch (Codec.CodecException | OntologyException e) {

                                e.printStackTrace();
                            }
                        } else {
                            block();
                        }
                    }

                }
            });

    }
    protected void takeDown() {
    }
}
