import tmf.host.UserPasswordEncoderListener
import tmf.host.security.AuthFailListener
import tmf.host.security.AuthSuccessListener
import tmf.host.security.TfamSecurity

// Place your Spring DSL code here
beans = {

    userPasswordEncoderListener(UserPasswordEncoderListener)


    authFailureListener(AuthFailListener.class)
    authSuccessListener(AuthSuccessListener.class)


    // Gives methods for using in @Secured annotation.
    tfamSecurity(TfamSecurity.class)

}
