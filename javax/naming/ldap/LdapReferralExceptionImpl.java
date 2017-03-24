package javax.naming.ldap;
public class LdapReferralExceptionImpl extends LdapReferralException{
	protected LdapReferralExceptionImpl( java.lang.String arg0) {super(arg0);}
	protected LdapReferralExceptionImpl() {super();}
	public javax.naming.Context getReferralContext( java.util.Hashtable arg0){return null;}
	public javax.naming.Context getReferralContext( java.util.Hashtable arg0,  javax.naming.ldap.Control[] arg1){return null;}
	public javax.naming.Context getReferralContext(){return null;}
	public java.lang.Object getReferralInfo(){return null;}
	public void retryReferral(){return;}
	public boolean skipReferral(){return true;}
}
