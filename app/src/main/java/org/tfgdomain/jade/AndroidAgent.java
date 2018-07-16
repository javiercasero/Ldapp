package org.tfgdomain.jade;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.unboundid.ldap.sdk.ResultCode;

import org.tfgdomain.ldapp.UserActivity;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.gateway.GatewayAgent;

public class AndroidAgent extends GatewayAgent {
    private static final long serialVersionUID = 1594371294421614291L;
    private Context context;
    private Codec codec = new SLCodec();
    private Ontology ontology = LdapOntology.getInstance();
    private ACLMessage jadeMsg;
    private String domain, user;
    //private final ContentManager cm = getContentManager();

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
		/*
        ACLMessage mensaje = new ACLMessage(ACLMessage.PROPOSE);
        //AID remote = new AID("ams@10.15.149.10:1099/JADE", AID.ISGUID);
        //remote.addAddresses("http://10.0.2.2:7778/acc");
        AID remote = new AID("ams@10.15.149.83:1099/JADE", AID.ISGUID);
        remote.addAddresses("http://INF-JCASERO-W10.riojasalud.es:7778/acc");
        mensaje.setContent("Hola Mundo");
        mensaje.addReceiver(remote);
        send(mensaje);*/


        // Build the description used as template for the search




  		/*SearchConstraints sc = new SearchConstraints();
  		// We want to receive 10 results at most
  		sc.setMaxResults(new Long(1));
  		//Para buscar en plataformas federadas
  		//sc.setMaxDepth(new Long(1));
  		*/



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
                        //account.setUser("CN=Usuario 1,OU=Usuarios,OU=Test,DC=tfgdomain,DC=org");
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

                    //ACLMessage aclMsg = receive(mt);
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
                                        //broadcast.putExtra("prueba", "desbloqueo correcto");
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


/*




    }
    /*
    @Override
    protected void afterMove() {
        super.afterMove();
        registerLangAndOnto();
    }

    private void registerLangAndOnto() {

        getContentManager().registerLanguage(codec);

        getContentManager().registerOntology(ontology);

    */
    }
    protected void takeDown() {
    }
}
