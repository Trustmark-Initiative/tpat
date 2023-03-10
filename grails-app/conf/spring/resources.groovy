import tmf.host.security.TfamSecurity

// Place your Spring DSL code here
beans = {

    // Gives methods for using in @Secured annotation.
    tfamSecurity(TfamSecurity.class)

}
