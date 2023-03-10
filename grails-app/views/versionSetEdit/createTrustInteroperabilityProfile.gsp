<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>

<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <title>${grailsApplication.config.tf.org.toolheader} | TIP Editor</title>

    <meta charset="utf-8"/>

    <style type="text/css">
    #mainContainer {
        margin-top: 3em;
    }


    .keywordLabel {
        font-size: 110%;
        margin-right: 1em;
        margin-bottom: 0.5em;
        display: inline-block;
    }
    .keywordRemoveLink, .keywordRemoveLink:active, .keywordRemoveLink:visited {
        color: inherit;
    }

    .providerRefContainer {
        margin-left: 1em;
        font-size: 90%;
    }

    </style>
    <script type="text/javascript">
        var SAMPLE_SEARCH_RESPONSE = {"queryString":"loa 3","terms":["loa","3"],"tdCountTotal":599,"tipCountTotal":229,"versionSetId":"VS_20170218","results":{"tdCount":3,"tipCount":82,"tds":[{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trustmark-definitions/nist-800-63-loa-3-derived-credential-issuance/1.0/","Name":"NIST 800-63 LOA 3 Derived Credential Issuance","Version":"1.0","Description":"This Trustmark Definition covers the issuance of derived credentials by Credential Service Providers (CSPs) at NIST 800-63 LOA 3.","Deprecated":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trustmark-definitions/nist-800-63-loa-2-derived-credential-issuance/1.0/","Name":"NIST 800-63 LOA 2 Derived Credential Issuance","Version":"1.0","Description":"This Trustmark Definition covers the issuance of derived credentials by Credential Service Providers (CSPs) at NIST 800-63 LOA 2.","Deprecated":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trustmark-definitions/nist-800-63-loa-4-derived-credential-issuance/1.0/","Name":"NIST 800-63 LOA 4 Derived Credential Issuance","Version":"1.0","Description":"This Trustmark Definition covers the issuance of derived credentials by Credential Service Providers (CSPs) at NIST 800-63 LOA 4.","Deprecated":false}],"tips":[{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/ficam-loa-3-profile/1.0/","Name":"FICAM LOA 3 Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies the requirements for a CSP to meet FICAM LOA 3.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-profile/1.0/","Name":"NIST 800-63 LOA 3 Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies the requirements for a CSP to meet NIST 800-63 LOA 3.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-registration-and-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 Registration and Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on subscriber registration and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-registration-and-issuance-for-financial-institutions-profile/1.0/","Name":"NIST 800-63 LOA 3 Registration and Issuance for Financial Institutions Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on CSP registration of subscribers and credential issuance, for CSPs that are financial institutions that are required, by government oversight such as the Bank Secrecy Act, the USA Patriot Act, the Office of the Comptroller of the Currency, the Federal Financial Institutions Examination Council, or the Securities  and Exchanges Commission, to implement a \"Customer Identification Program\".","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-in-person-identity-proofing-and-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 In-Person Identity Proofing and Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on in-person identity proofing of registration applicants and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-remote-identity-proofing-and-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 Remote Identity Proofing and Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on remote identity proofing of registration applicants and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-derived-credential-profile/1.0/","Name":"NIST 800-63 LOA 3 Derived Credential Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 requirements for issuance of LOA 3 derived credentials by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/ficam-loa-3-registration-and-issuance-profile/1.0/","Name":"FICAM LOA 3 Registration and Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies FICAM LOA 3 requirements on subscriber registration and credential issuance by CSPs. This TIP is equivalent to the NIST 800-63 LOA 3 Registration and Issuance Profile, with the sole exception being that this TIP does not allow the use of derived credentials.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 3 Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-in-person-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 3 In-Person Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on in-person identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-remote-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 3 Remote Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on remote identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-in-person-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 In-Person Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on in-person credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-remote-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 Remote Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on remote credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-credential-issuance-by-employers-of-licensed-professionals-profile/1.0/","Name":"NIST 800-63 LOA 3 Credential Issuance by Employers of Licensed Professionals Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on credential issuance by CSPs, where the subscribers are licensed professionals and employees or affiliates of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-applicant-tracking-profile/1.0/","Name":"NIST 800-63 LOA 3 Applicant Tracking Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on in-person and remote tracking of registration applicants by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-and-4-in-person-applicant-tracking-profile/1.0/","Name":"NIST 800-63 LOA 3 and 4 In-Person Applicant Tracking Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 and 4 requirements on in-person tracking of registration applicants by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-token-strength-and-authentication-profile/1.0/","Name":"NIST 800-63 LOA 3 Token Strength and Authentication Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 token strength requirements, and token-specific authentication requirements on CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-multi-factor-software-cryptographic-token-strength-and-authentication-profile/1.0/","Name":"NIST 800-63 LOA 3 Multi-Factor Software Cryptographic Token Strength and Authentication Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on the implementation and strength of multi-factor cryptographic tokens, and the use of these tokens for subscriber authentication by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-token-and-credential-management-profile/1.0/","Name":"NIST 800-63 LOA 3 Token and Credential Management Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on the management of authentication tokens and credentials by CSPs.\n\nCredential storage requirements from NIST 800-63 are not covered in this TIP, but are covered in the NIST 800-63 Token Type Specific Strength and Authentication Profiles, specifically for shared secret tokens.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-renewal-and-re-issuance-profile/1.0/","Name":"NIST 800-63 LOA 3 Renewal and Re-Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on the renewal and re-issuance of tokens and credentials by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/ficam-loa-3-token-and-credential-management-profile/1.0/","Name":"FICAM LOA 3 Token and Credential Management Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on the management of authentication tokens and credentials by CSPs, as adopted by FICAM. This Profile does not the include security control requirements stated in NIST 800-63.\n\nCredential storage requirements from NIST 800-63 are not covered in this TIP, but are covered in the NIST 800-63 Token Type Specific Strength and Authentication Profiles, specifically for shared secret tokens.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-authentication-process-profile/1.0/","Name":"NIST 800-63 LOA 3 Authentication Process Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on subscriber authentication by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-3-identity-proofing-and-credential-issuance-by-employers-of-licensed-professionals-profile/1.0/","Name":"NIST 800-63 LOA 3 Identity Proofing and Credential Issuance by Employers of Licensed Professionals Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on identity proofing of registration applicants and credential issuance by CSPs, where the subscribers are licensed professionals and employees or affiliates of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-identity-proofing-and-credential-issuance-by-employers-of-licensed-professionals-profile/1.0/","Name":"NIST 800-63 LOA 4 Identity Proofing and Credential Issuance by Employers of Licensed Professionals Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 3 requirements on identity proofing of registration applicants and credential issuance by CSPs, where the subscribers are licensed professionals and employees or affiliates of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/ficam-loa-2-profile/1.0/","Name":"FICAM LOA 2 Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies the requirements for a CSP to meet FICAM LOA 2.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-profile/1.0/","Name":"NIST 800-63 LOA 2 Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies the requirements for a CSP to meet NIST 800-63 LOA 2.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-profile/1.0/","Name":"NIST 800-63 LOA 4 Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies the requirements for a CSP to meet NIST 800-63 LOA 4.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-registration-and-issuance-profile/1.0/","Name":"NIST 800-63 LOA 2 Registration and Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on subscriber registration and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-registration-and-issuance-profile/1.0/","Name":"NIST 800-63 LOA 4 Registration and Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on subscriber registration and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-registration-and-issuance-for-financial-institutions-profile/1.0/","Name":"NIST 800-63 LOA 2 Registration and Issuance for Financial Institutions Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on CSP registration of subscribers and credential issuance, for CSPs that are financial institutions that are required, by government oversight such as the Bank Secrecy Act, the USA Patriot Act, the Office of the Comptroller of the Currency, the Federal Financial Institutions Examination Council, or the Securities  and Exchanges Commission, to implement a \"Customer Identification Program\".","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-registration-and-issuance-for-financial-institutions-profile/1.0/","Name":"NIST 800-63 LOA 4 Registration and Issuance for Financial Institutions Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on CSP registration of subscribers and credential issuance, for CSPs that are financial institutions that are required, by government oversight such as the Bank Secrecy Act, the USA Patriot Act, the Office of the Comptroller of the Currency, the Federal Financial Institutions Examination Council, or the Securities  and Exchanges Commission, to implement a \"Customer Identification Program\".","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-in-person-identity-proofing-and-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 2 In-Person Identity Proofing and Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on in-person identity proofing of registration applicants and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-remote-identity-proofing-and-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 2 Remote Identity Proofing and Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on remote identity proofing of registration applicants and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-identity-proofing-and-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 4 Identity Proofing and Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on identity proofing of registration applicants and credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-identity-proofing-and-credential-issuance-by-employers-and-educational-institutions-profile/1.0/","Name":"NIST 800-63 LOA 2 Identity Proofing and Credential Issuance by Employers and Educational Institutions Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on identity proofing of registration applicants and credential issuance by CSPs, where the subscribers are employees or students of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-identity-proofing-and-credential-issuance-by-employers-of-licensed-professionals-profile/1.0/","Name":"NIST 800-63 LOA 2 Identity Proofing and Credential Issuance by Employers of Licensed Professionals Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on identity proofing of registration applicants and credential issuance by CSPs, where the subscribers are licensed professionals and employees or affiliates of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-derived-credential-profile/1.0/","Name":"NIST 800-63 LOA 2 Derived Credential Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 requirements for issuance of LOA 2 derived credentials by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-derived-credential-profile/1.0/","Name":"NIST 800-63 LOA 4 Derived Credential Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 requirements for issuance of LOA 4 derived credentials by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/ficam-loa-4-registration-and-issuance-profile/1.0/","Name":"FICAM LOA 4 Registration and Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on subscriber registration and credential issuance by CSPs. This TIP is equivalent to the NIST 800-63 LOA 4 Registration and Issuance Profile, with the sole exception being that this TIP does not allow the use of derived credentials.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 2 Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 4 Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-in-person-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 2 In-Person Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on in-person identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-remote-identity-proofing-profile/1.0/","Name":"NIST 800-63 LOA 2 Remote Identity Proofing Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on remote identity proofing done by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-in-person-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 2 In-Person Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on in-person credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-remote-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 2 Remote Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on remote credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-4-credential-issuance-profile/1.0/","Name":"NIST 800-63 LOA 4 Credential Issuance Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 4 requirements on credential issuance by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-credential-issuance-by-employers-of-licensed-professionals-profile/1.0/","Name":"NIST 800-63 LOA 2 Credential Issuance by Employers of Licensed Professionals Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on credential issuance by CSPs, where the subscribers are licensed professionals and employees or affiliates of the CSP.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-applicant-tracking-profile/1.0/","Name":"NIST 800-63 LOA 2 Applicant Tracking Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on in-person and remote tracking of registration applicants by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-in-person-applicant-tracking-profile/1.0/","Name":"NIST 800-63 LOA 2 In-Person Applicant Tracking Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 requirements on in-person tracking of registration applicants by CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-1-token-strength-and-authentication-profile/1.0/","Name":"NIST 800-63 LOA 1 Token Strength and Authentication Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 1 token strength requirements, and token-specific authentication requirements on CSPs.","Deprecated":false,"primary":false},{"Identifier":"https://trustmark.someorgoutthere.com/operational-pilot/trust-interoperability-profiles/nist-800-63-loa-2-token-strength-and-authentication-profile/1.0/","Name":"NIST 800-63 LOA 2 Token Strength and Authentication Profile","Version":"1.0","Description":"This Trust Interoperability Profile specifies NIST 800-63 LOA 2 token strength requirements, and token-specific authentication requirements on CSPs.","Deprecated":false,"primary":false}]}};

        var SEARCH_URL = '${SEARCH_URL}';

        var TIP = null;

        var PROVIDERS = [];


        /**
         * Javascript entry point, called after all initial data has been loaded and the document is ready.
         */
        $(document).ready(function(){
            console.log("document.ready firing...");
            if (!String.prototype.trim) {
                String.prototype.trim = function () {
                    return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
                };
            }

            // ${raw(SET_TIP_HERE)}

            if( TIP == null )
                TIP = buildEmptyTIP();

            setTipRefrences(TIP);

            setTimeout('updateUI()', 500);
            setTimeout('displayContent()', 750);
            setTimeout('loadProviderReferences()', 50);
        });

        function setTipRefrences(tip){
            console.log("TIP References: \n"+JSON.stringify(tip.References, null, 4));
            TIP_REFERENCES = [];
            if( tip && tip.References && tip.References.TrustmarkDefinitionRequirements && tip.References.TrustmarkDefinitionRequirements.length > 0 ){
                for( var i = 0; i < tip.References.TrustmarkDefinitionRequirements.length; i++ ){
                    var tdReq = tip.References.TrustmarkDefinitionRequirements[i];
                    var ref = {
                        "$id": ""+tdReq["$id"],
                        "Type" : "TrustmarkDefinition",
                        Identifier: tdReq.TrustmarkDefinitionReference.Identifier,
                        Name: tdReq.TrustmarkDefinitionReference.Name,
                        Version: tdReq.TrustmarkDefinitionReference.Version,
                        Description: tdReq.TrustmarkDefinitionReference.Description,
                        Providers: expandProviders(tip, tdReq.ProviderReferences)
                    }
                    TIP_REFERENCES.push(ref);
                }
            }
            if( tip && tip.References && tip.References.TrustInteroperabilityProfileReferences && tip.References.TrustInteroperabilityProfileReferences.length > 0 ){
                for( var i = 0; i < tip.References.TrustInteroperabilityProfileReferences.length; i++ ){
                    var tipRef = tip.References.TrustInteroperabilityProfileReferences[i];
                    var ref = {
                        "$id": ""+tipRef["$id"],
                        "Type" : "TrustInteroperabilityProfile",
                        Identifier: tipRef.Identifier,
                        Name: tipRef.Name,
                        Version: tipRef.Version,
                        Description: tipRef.Description
                    }
                    TIP_REFERENCES.push(ref);
                }

            }
        }

        function expandProviders(tip, providers){
            var newProviders = [];
            if( providers && providers.length > 0 ){
                for( var i = 0; i < providers.length; i++ ){
                    var provider = providers[i];
                    if( provider['$id'] != null ){
                        newProviders.push(provider);
                    }else if( provider['$ref'] != null ){
                        var id = provider['$ref'].substring(1); // we remove the leading # symbol.
                        var found = false;
                        for( var j = 0; j < tip.References.TrustmarkDefinitionRequirements.length; j++ ) {
                            var tdReq = tip.References.TrustmarkDefinitionRequirements[j];
                            if( tdReq.ProviderReferences != null && tdReq.ProviderReferences.length > 0 ){
                                for( var k = 0; k < tdReq.ProviderReferences.length; k++ ){
                                    var curProvider = tdReq.ProviderReferences[k];
                                    if( curProvider['$id'] ){
                                        if( curProvider['$id'] === id ){
                                            newProviders.push(curProvider);
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if( found ){
                                    break;
                                }
                            }
                        }// end each TD requirement
                    }//end if this is a reference
                }// end each provider.
            }
            return newProviders;
        }

        /**
         * Loads the full list of provider refs from the server.
         */
        function loadProviderReferences(){
            console.log("Loading providers...");
            $.ajax({
                url: '${createLink(controller: 'provider', action: 'list')}',
                dataType: 'json',
                data: {
                    timestamp: new Date().getTime(),
                    format: 'json',
                    max: 10000
                },
                success: function(data){
                    if( data && data.providers ) {
                        PROVIDERS = data.providers;
                        console.log("Successfully loaded "+PROVIDERS.length+" providers.");
                    }
                },
                error: function(jqXHR, textStatus, errorThrown){
                    // Do nothing here, we just silently fail.
                }
            })
        }

        /**
         * Menu Callback to calculate the JSON represented currently and display it.
         */
        function menuFileDownloadJSON() {
            saveDataToJSON();
            var jsonString = JSON.stringify(TIP, null, 4);
            $('#jsonView').html('<div style="margin-top: 4em;"><h1>Raw JSON: </h1><pre>'+htmlEncode(jsonString)+'</pre></div><div style="margin-top: 1em;"><a class="btn btn-default" href="javascript:clearJson()">Clear</a></div>');
        }//end menuFileDownloadJSON()

        function clearJson(){
            $('#jsonView').html('');
        }


        /**
         * Menu callback to validate the current data and make sure everything required is present.  Calls the "doValidation()"
         * method to do the heavy lifting, a method that can be leveraged by other logic.
         */
        function doValidationMenuItem() {
            if( doValidation() ){
                $('#feedbackWindow').html('<div class="alert alert-success">This Trust Interoperability Profile validates successfully.</div>');
            }
        }


        /**
         * A helper for testing
         */
        function menuAutofillTestData() {
            $('#tipName').val('Password Complexity Requirements');
            $('#tipPublicationDate').val('2017-01-17');
            $('#tipVersion').val('1.0-SNAPSHOT');
            $('#identifier').val('https://artifacts.trustmarkinitiative.org/lib/trust-interoperability-profiles/password-complexity-requirements/1.0-SNAPSHOT/');

            $('#tipIssuerName').val('Georgia Tech Research Institute');
            $('#tipIssuerId').val('https://trustmarks.gtri.org/');

            $('#tipIssuerResponder').val('Trustmark Framework');
            $('#tipIssuerEmail').val('TrustmarkFeedback@gtri.gatech.edu');
            $('#tipDescription').val('This is a TIP Description, for the purposes of testing only!');


        }


        function displayMetadata(){
            displayTab('tabsMetadata');
        }

        function displayContent() {
            displayTab('tabsContent');
        }

        function displayTab(id){
            console.log("Showing Tab id="+id);
            $('#'+id+' a').tab('show');
        }


        /**
         * Performs validation in javascript, returning true if valid and false if not.  If not, then the page is updated
         * to reflect the error.
         */
        function doValidation() {
            console.log("Performing in-page validation checks...");
            clearFeedback();
            saveDataToJSON();

            if( TIP.Name.trim().length == 0 ){
                setValidationError("Name must not be empty.");
                displayMetadata();
                $('#tipName').focus();
                return false;
            }

            if( TIP.PublicationDateTime.trim().length == 0 ){
                setValidationError("Publication Date must not be empty.");
                displayMetadata();
                $('#tipPublicationDate').focus();
                return false;
            }
            var pubDate = Date.parse(TIP.PublicationDateTime);
            console.log("Date timestamp = "+pubDate);
            if( pubDate == null || isNaN(pubDate) ){
                setValidationError("Publication Date must match yyyy-mm-dd, like 2017-01-16");
                displayMetadata();
                $('#tipPublicationDate').focus();
                return false;
            }


            if( TIP.Version.trim().length == 0 ){
                setValidationError("Version must not be empty.");
                displayMetadata();
                $('#tipVersion').focus();
                return false;
            }

            if( TIP.Identifier.trim().length == 0 ){
                setValidationError("Identifier must not be empty.");
                displayMetadata();
                $('#identifier').focus();
                return false;
            }
            if( !validateURL(TIP.Identifier) ){
                setValidationError("Identifier must be a valid URL (ie, internet address).");
                displayMetadata();
                $('#identifier').focus();
                return false;
            }

            if( TIP.Issuer.Name.trim().length == 0 ){
                setValidationError("Issuer Organization Name must not be empty.");
                displayMetadata();
                $('#tipIssuerName').focus();
                return false;
            }
            if( TIP.Issuer.Identifier.trim().length == 0 ){
                setValidationError("Issuer Organization URI must not be empty.");
                displayMetadata();
                $('#tipIssuerId').focus();
                return false;
            }

            if( TIP.Issuer.PrimaryContact.Email.trim().length == 0 ){
                setValidationError("Issuer Organization Email must not be empty.");
                displayMetadata();
                $('#tipIssuerEmail').focus();
                return false;
            }
            if( !validateEmail(TIP.Issuer.PrimaryContact.Email.trim()) ){
                setValidationError("Issuer Organization Email must be a valid email address, ie somebody@somewhere.com");
                displayMetadata();
                $('#tipIssuerEmail').focus();
                return false;
            }
            if( TIP.Description.trim().length == 0 ){
                setValidationError("Description must not be empty.");
                displayMetadata();
                $('#tipDescription').focus();
                return false;
            }

            if( isBlank(TIP.TrustExpression) ){
                setValidationError("Trust Expression must not be empty.");
                displayContent();
                $('#trustExpression').focus();
                return false;
            }

            if( TIP.References == null || (!hasTDRequirements(TIP) && !hasTIPReferences(TIP) ) ){
                setValidationError("TD or TIP References are required.");
                displayContent();
                return false;
            }

            return true;
        }//end doValidation()

        /**
         * Given a TIP, this method will return true if the TIP has TIP References in the References section.
         */
        function hasTIPReferences(tip){
            return tip && tip.References && tip.References.TrustInteroperabilityProfileReferences && tip.References.TrustInteroperabilityProfileReferences.length > 0;
        }

        /**
         * Given a TIP, this method will return true if the TIP has TD Requirements in the References section
         */
        function hasTDRequirements(tip){
            return tip && tip.References && tip.References.TrustmarkDefinitionRequirements && tip.References.TrustmarkDefinitionRequirements.length > 0;
        }

        // Validates URL values
        function validateURL(str) {
            // @see https://stackoverflow.com/a/22648406/563328
            var urlRegex = '^(?!mailto:)(?:(?:http|https|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$';
            var url = new RegExp(urlRegex, 'i');
            return str.length < 2083 && url.test(str);
        }

        // Validates Email Values
        function validateEmail(email) {
            var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return re.test(email);
        }

        // Simply clears the feedback window at the top of the page.
        function clearFeedback() {
            $('#feedbackWindow').html('');
        }

        // Displays an error message to the user.
        function setValidationError(msg) {
            $('#feedbackWindow').html('<div class="alert alert-danger">'+msg+'</div>');
        }


        // Taken from http://stackoverflow.com/questions/1219860/html-encoding-in-javascript-jquery
        function htmlEncode(value){
            //create a in-memory div, set it's inner text(which jQuery automatically encodes)
            //then grab the encoded contents back out.  The div never exists on the page.
            return $('<div/>').text(value).html();
        }


        /**
         * When called, this method will write the fields in the User Interface back to the TIP object, and the TIP
         * object is then capable of being sent to the server.
         */
        function saveDataToJSON() {
            if( TIP == null )
                TIP = {};

            TIP.Name                = $('#tipName').val();
            TIP.PublicationDateTime = $('#tipPublicationDate').val();
            TIP.Version             = $('#tipVersion').val();
            TIP.Identifier          = $('#identifier').val();
            TIP.Description         = $('#tipDescription').val();
            TIP.LegalNotice         = $('#LegalNotice').val();
            TIP.Notes               = $('#Notes').val();

            TIP.Issuer = {};
            TIP.Issuer.Name         = $('#tipIssuerName').val();
            TIP.Issuer.Identifier   = $('#tipIssuerId').val();

            TIP.Issuer.OtherContacts = null;

            TIP.Issuer.PrimaryContact                = {};
            TIP.Issuer.PrimaryContact.Kind           = "PRIMARY";
            TIP.Issuer.PrimaryContact.Responder      = $('#tipIssuerResponder').val();
            TIP.Issuer.PrimaryContact.Email          = $('#tipIssuerEmail').val();
            TIP.Issuer.PrimaryContact.Telephone      = $('#tipIssuerPhone').val();
            TIP.Issuer.PrimaryContact.MailingAddress = $('#tipIssuerMailingAddr').val();
            TIP.Issuer.PrimaryContact.Notes          = $('#tipIssuerNotes').val();

            // TODO Set TIP.Keywords from UI?  This is already being managed in other places.
            // TODO Set TIP.Deprecated from UI?  This is already being managed in other places.

            TIP.Supersessions = null;

            var supersedes = $('#Supersedes').val();
            if( isNotBlank(supersedes) ){
                TIP.Supersessions = {};
                TIP.Supersessions.Supersedes = [];
                var supersedesArray = supersedes.split(/\s+/);
                for( var i = 0; i < supersedesArray.length; i++ ){
                    var element = supersedesArray[i];
                    if( isNotBlank(element) ){
                        TIP.Supersessions.Supersedes.push({"Identifier" : element});
                    }
                }
            }

            if( TIP.Deprecated ) {
                var supersededBy = $('#supersededBy').val();
                if (isNotBlank(supersededBy)) {
                    if (TIP.Supersessions == null)
                        TIP.Supersessions = {};
                    TIP.Supersessions.SupersededBy = [];
                    var supersededByArray = supersededBy.split(/\s+/);
                    for (var i = 0; i < supersededByArray.length; i++) {
                        var element = supersededByArray[i];
                        if (isNotBlank(element)) {
                            TIP.Supersessions.SupersededBy.push({"Identifier": element});
                        }
                    }
                }
            }else{
                // TODO - Do we disregard this value if we are not deprecated?
            }

            TIP.TrustExpression = $('#trustExpression').val();

            parseProviders(TIP_REFERENCES);
            TIP.References = {TrustmarkDefinitionRequirements: [], TrustInteroperabilityProfileReferences: []};
            var providersEncountered = {};
            for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                var ref = TIP_REFERENCES[i];
                if( ref.Type === "TrustmarkDefinition" ){
                    var providers = ref.Providers;
                    providers = buildProvidersByIdOrRef(providersEncountered, providers);
                    TIP.References.TrustmarkDefinitionRequirements.push({'$id': ""+ref['$id'], 'TrustmarkDefinitionReference' : ref, 'ProviderReferences' : providers});
                }else{
                    TIP.References.TrustInteroperabilityProfileReferences.push(ref);
                }
            }

        }//end saveJson()

        function buildProvidersByIdOrRef(encountered, current){
            var modified = []
            if( current && current.length > 0 ) {
                for (var i = 0; i < current.length; i++) {
                    var provider = current[i];
                    var hash = provider.Identifier.hashCode();
                    if (encountered[hash] != null) {
                        modified.push({"$ref": ""+hash});
                    } else {
                        var obj = {"$id": ""+hash, Identifier: provider.Identifier} // TODO Fill in other fields?
                        modified.push(obj);
                        encountered[hash] = obj;
                    }
                }
            }
            return modified;
        }


        function parseProviders(references){
            for( var i = 0; i < references.length; i++ ){
                var ref = references[i];
                var providers = [];
                var providerSelects = $('#providerContainer'+ref.Identifier.hashCode()+' .providerSelect');
                for( var j = 0; j < providerSelects.length; j++ ){
                    var select = providerSelects[j];
                    var selectedValue = $(select).val();
                    if( selectedValue.trim().length > 0 ) {
                        console.log("For TD: " + ref.Name + ", found Select: [" + selectedValue + "]");
                        providers.push({Identifier: selectedValue});
                    }
                }
                if( providers.length > 0 ){
                    ref.Providers = providers;
                }
            }
        }


        /**
         * Has the effect of creating a base, empty structure for the TIP.
         */
        function buildEmptyTIP(){
            return {
                "$TMF_VERSION" : "1.1",
                "$Type" : "TrustInteroperabilityProfile",
                "$id": "",
                "Identifier": "",
                "Name": "",
                "Version": "1.0-SNAPSHOT",
                "PublicationDateTime": "",
                "Deprecated": false,
                "Description": "",
                "Keywords": [],
                "Issuer": {
                    "Identifier": "",
                    "Name": "",
                    "PrimaryContact": {
                        "Kind": "PRIMARY",
                        "Responder": "",
                        "Email": ""
                    }
                },
                "TrustExpression": "true",
                "References": {
                    "TrustInteroperabilityProfileReferences": [],
                    "TrustmarkDefinitionRequirements": []
                },
                "LegalNotice": "",
                "Notes": ""
            };
        }

        /**
         * Responsible for assigning the data from the given TIP JSON to the user interface.
         */
        function updateUI(){
            updateMetadata(TIP);
            updateDeprecated(TIP);
            updateContent(TIP);
        }

        /**
         * Updates the UI Text boxes to display the given TIP metadata.
         */
        function updateMetadata(tip) {
            console.log("Updating UI to match TIP for Metadata...");

            $('#tipName').val(valOrEmpty(tip.Name));
            $('#tipPublicationDate').val(valOrEmpty(tip.PublicationDateTime));
            $('#tipVersion').val(valOrEmpty(tip.Version));
            $('#identifier').val(valOrEmpty(tip.Identifier));
            $('#tipDescription').val(valOrEmpty(tip.Description));

            $('#tipIssuerName').val(valOrEmpty(tip.Issuer.Name));
            $('#tipIssuerId').val(valOrEmpty(tip.Issuer.Identifier));

            $('#LegalNotice').val(valOrEmpty(tip.LegalNotice));
            $('#Notes').val(valOrEmpty(tip.Notes));

            updateKeywords(tip);


            var contact = null;
            if( tip && tip.Issuer && tip.Issuer.PrimaryContact ){
                contact = tip.Issuer.PrimaryContact;
            }else{
                // TODO Any other way to fill contact?
            }
            if( contact ){
                $('#tipIssuerResponder').val(valOrEmpty(contact.Responder));
                $('#tipIssuerEmail').val(valOrEmpty(getFirstEmail(contact)));
                $('#tipIssuerPhone').val(valOrEmpty(getFirstTelephone(contact)));
                $('#tipIssuerMailingAddr').val(valOrEmpty(getFirstMailingAddress(contact)));
                $('#tipIssuerNotes').val(valOrEmpty(contact.Notes));
            }else{
                $('#tipIssuerResponder').val('');
                $('#tipIssuerEmail').val('');
                $('#tipIssuerPhone').val('');
                $('#tipIssuerMailingAddr').val('');
                $('#tipIssuerNotes').val('');
            }


            if( tip.Supersessions && tip.Supersessions.Supersedes && tip.Supersessions.Supersedes.length > 0 ){
                var supersedesString = '';
                for( var i = 0; i < tip.Supersessions.Supersedes.length; i++ ){
                    supersedesString += tip.Supersessions.Supersedes[i].Identifier;
                    if( i < (tip.Supersessions.Supersedes.length - 1) ){
                        supersedesString += '\n';
                    }
                }
                $('#Supersedes').val(supersedesString);
            }

            if( tip.Supersessions && tip.Supersessions.SupersededBy && tip.Supersessions.SupersededBy.length > 0 ){
                var supersededByString = '';
                for( var i = 0; i < tip.Supersessions.SupersededBy.length; i++ ){
                    supersededByString += tip.Supersessions.SupersededBy[i].Identifier;
                    if( i < (tip.Supersessions.SupersededBy.length - 1) ){
                        supersededByString += '\n';
                    }
                }
                $('#supersededBy').val(supersededByString);
            }

        }

        function getFirstEmail(contact){
            if( contact && contact.Email ){
                return contact.Email;
            }else if( contact && contact.Emails ){
                return contact.Emails[0];
            }else{
                return null;
            }
        }

        function getFirstMailingAddress(contact){
            if( contact && contact.MailingAddress ){
                return contact.MailingAddress;
            }else if( contact && contact.MailingAddresses ){
                return contact.MailingAddresses[0];
            }else{
                return null;
            }
        }

        function getFirstTelephone(contact){
            if( contact && contact.Telephone ){
                return contact.Telephone;
            }else if( contact && contact.Telephones ){
                return contact.Telephones[0];
            }else{
                return null;
            }
        }

        //==========================================================================================================
        // Deprecated Changes
        //==========================================================================================================
        /**
         * Called to switch the value of DEPRECATED, and update the view.
         */
        function toggleDeprecated() {
            TIP.Deprecated = !TIP.Deprecated;
            updateDeprecated(TIP);
        }

        /**
         * Analyzes the "DEPRECATED" boolean and updates the view accordingly.
         */
        function updateDeprecated(tip) {
            if( tip.Deprecated ){
                $('#deprecatedIndicator').addClass('glyphicon-ok');
                $('#deprecatedIndicator').removeClass('glyphicon-remove');
                $('#deprecatedText').html("Deprecated");
                $('#supersedByContainer').show();
            }else{
                $('#deprecatedIndicator').removeClass('glyphicon-ok');
                $('#deprecatedIndicator').addClass('glyphicon-remove');
                $('#deprecatedText').html("Not Deprecated");
                $('#supersedByContainer').hide();
            }
        }//end updateDeprecated()

        //==========================================================================================================
        // Keywords
        //==========================================================================================================
        /**
         * Returns a sorted version of the Keywords Array.
         */
        function getSortedKeywords(tip){
            if( !tip )
                tip = {};
            if( !tip.Keywords )
                tip.Keywords = [];
            tip.Keywords.sort(sortStrings);
            return tip.Keywords;
        }//end getSortedKeywords()
        /**
         * Will prompt the user for and add a new keyword to the list.
         */
        function addNewKeyword(){
            var keyword = prompt("Please enter your new keyword: ");
            if (/\S/.test(keyword)) {
                if( TIP.Keywords && TIP.Keywords.length > 0 ){
                    for( var i = 0; i < TIP.Keywords.length; i++ ){
                        if( keyword == TIP.Keywords[i] ){
                            alert("That keyword already exists.");
                            return;
                        }
                    }
                }else{
                    TIP.Keywords = []
                }
                console.log("Adding keyword: "+keyword);
                TIP.Keywords.push(keyword);
                updateKeywords(TIP);
            }else{
                alert("Keyword must have a value.");
            }
        }//end addNewKeyword()
        /**
         * Removes the value at in the KEYWORDS array at the given index.  If the index has a bad value, then no changes
         * will occur.
         */
        function removeKeyword(index){
            var newKeywords = new Array();
            if( TIP.Keywords && TIP.Keywords.length > 0 ){
                for( var i = 0; i < TIP.Keywords.length; i++ ){
                    if( i != index ){
                        newKeywords.push(TIP.Keywords[i]);
                    }else{
                        console.log("Removing keyword: "+TIP.Keywords[i]);
                    }
                }
            }
            TIP.Keywords = newKeywords;
            updateKeywords(TIP);
        }//end removeKeyword()
        /**
         * Updates the view associated with keywords.
         */
        function updateKeywords(tip){
            var html = '';
            var keys = getSortedKeywords(tip);
            if( keys != null && keys.length > 0 ){
                for( var index = 0; index < keys.length; index++ ){
                    var nextKeyword = keys[index];
                    html += '<span class="label label-default keywordLabel">'+nextKeyword+' | <a href="javascript:removeKeyword('+index+')" class="keywordRemoveLink" title="Click to remove this keyword."><span class="glyphicon glyphicon-remove"></span></a></span>'
                }
            }else{
                html += '<em>There are no keywords.</em>'
            }
            $('#keywordsContainer').html(html);
        }//end updateKeywords()

        //==========================================================================================================
        // Reference Methods
        //==========================================================================================================


        /**
         * Called to update a Reference Variable name.
         */
        function editReferenceVariable(varName){
            console.log("Reference Variable To Edit: "+varName);
            var newName = prompt("Please enter a new name to replace variable '"+varName+"': ", varName);
            if(isNotBlank(newName) ){
                if( /^[a-zA-Z_]\w*$/.test(newName) ){

                    if( doesTipContainReferenceVariable(TIP, newName) ){
                        alert("This variable name is already being used.  Not changing anything.");
                        return;
                    }


                    console.log("Changing ["+varName+"] to ["+newName+"]...");
                    if( hasTDRequirements(TIP) ){
                        for(var i = 0; i < REFERENCES.TrustmarkDefinitionRequirements.length; i++ ){
                            var tdReq = REFERENCES.TrustmarkDefinitionRequirements[i];
                            if( tdReq["$id"] === varName ){
                                tdReq["$id"] = newName;
                                updateTDReferences(TIP);
                            }
                        }
                    }
                    if( hasTIPReferences(TIP) ){
                        for(var i = 0; i < REFERENCES.TrustInteroperabilityProfileReferences.length; i++ ){
                            var tipRef = REFERENCES.TrustInteroperabilityProfileReferences[i];
                            if( tipRef["$id"] === varName ){
                                tipRef["$id"] = newName;
                                updateTIPReferences(TIP);
                            }
                        }
                    }

                    TIP.TrustExpression = replaceInTrustExpression($('#trustExpression').val(), varName, newName);
                    updateTrustExpression(TIP);

                }else{
                    console.log("New name is not a valid variable name (special characters)!");
                    alert("Not changing anything - the name you entered is invalid.  Your variable name must start with an alpha and subsequently contain ONLY alpha-numerics.  NO SPECIAL CHARACTERS ARE ALLOWED.");
                }

            }else{
                alert("Not changing anything, since you didn't enter a new name.");
            }
        }

        /**
         * replaceInTrustExpression.
         */
        function replaceInTrustExpression(string, varName, replacement){
            if( !string )
                return "";
            var indexLocations = new Array();
            for (var i = 0; i < string.length; i++) {
                if( containsVar(string, varName, i) ){
                    indexLocations.push(i);
                }
            }

            var newS = '';
            for( var i = 0; i < string.length; i++ ){
                if( arrayContains(indexLocations, i) ){
                    newS += replacement;
                    i += (varName.length - 1);
                }else{
                    newS += string.charAt(i);
                }
            }

            return newS;
        }

        /**
         * True IFF the value given is in the array somewhere (by using double equals)
         */
        function arrayContains(array, value){
            if( array && array.length > 0 ){
                for( var i = 0; i < array.length; i++ ){
                    if( array[i] == value )
                        return true;
                }
            }
            return false;
        }

        /**
         * Returns true if the string contains the given variable name at the given index, but false if it is followed
         * by a word character (ie, [a-Z0-9_] ).  So, some examples:
         *   1) containsVar('test', 'test', 0) => true
         *   2) containsVar('abc', 'a', 0) => false
         *   3) containsVar('a bc', 'a', 0) => true
         *   4) containsVar('td11 and td1 and td112', 'tdr1', 0) => false
         *   5) containsVar('td11 and td1 and td112', 'tdr1', 8) => true
         */
        function containsVar(string, varName, i){
            if( string.charAt(i) == varName.charAt(0) ){
                if (string.length >= (i+varName.length) ){
                    var sub = string.substring(i, i+varName.length);
                    if( sub === varName ){
                        if( string.length > (i+varName.length) ){
                            var charAfter = string.charAt(i+varName.length);
                            if( /^\w$/.test(charAfter) ){
                                return false;
                            }else{
                                return true;
                            }
                        }else{
                            return true;
                        }
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }//end containsVar()

        /**
         * Updates the Content page based on the given TIP information.
         */
        function updateContent(tip){
            updateTrustExpression(tip);
            updateTipReferenceView();
        }

        /**
         * Updates the TRUST Expression based on the given TIP
         */
        function updateTrustExpression(tip){
            $('#trustExpression').val(valOrEmpty(tip.TrustExpression));
        }
        //==========================================================================================================
        // Helper Methods
        //==========================================================================================================
        /**
         * Returns true if the string has any meaningful, non-whitespace content.
         */
        function isNotBlank(str){
            return !isBlank(str);
        }

        /**
         * Returns true if the given string is null or has nothing but whitespace content (or is empty).
         */
        function isBlank(str){
            if (!str)
                return true;
            return str.trim().length == 0;
        }

        /**
         * Given a presumed string, returns the trimmed value if it exists, the empty string otherwise.  IE, asserts
         * that the given value is not null.
         */
        function valOrEmpty(val){
            if( val ){
                return (''+val).trim();
            }else{
                return '';
            }
        }

        /**
         * Assumes the incoming value is an ID in the DOM somewhere, uses JQuery to find it and get the val on it.
         * If not null, trims the value and returns it.  Otherwise, returns the empty string.
         */
        function idValOrEmpty(id){
            var val = $('#'+id).val();
            if( val )
                val = val.trim();
            else
                val = '';
            return val;
        }

        /**
         * Sorts the given array by name.
         */
        function sortByName(a, b){
            var aName = a.Name.toLowerCase();
            var bName = b.Name.toLowerCase();
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Sorts the given array by Identifier
         */
        function sortByIdentifier(a, b){
            var aName = a.Identifier.toLowerCase();
            var bName = b.Identifier.toLowerCase();
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Sorts the given array by Number.
         */
        function sortByNumber(a, b){
            var aName = a.Number;
            var bName = b.Number;
            return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
        }

        /**
         * Performs a sort algorithm for two incoming strings.
         */
        function sortStrings(s1, s2){
            var s1Lower = s1.toLowerCase();
            var s2Lower = s2.toLowerCase();
            return ((s1Lower < s2Lower) ? -1 : ((s1Lower > s2Lower) ? 1 : 0));
        }



        function saveTipToServer() {
            saveDataToJSON();
            var jsonString = JSON.stringify(TIP, null, 4);
            console.log("Saving TIP JSON: "+jsonString);
            if( !doValidation() )
                return;

            $.ajax({
                url: '${createLink(controller: 'versionSetEdit', action: 'saveTrustInteroperabilityProfile', id: vs.name, params: [linkId: linkId])}',
                dataType: 'json',
                method: 'POST',
                data: jsonString,
                contentType: 'application/json',
                success: function(result){
                    if( result && result.status ){
                        if( result.status === "SUCCESS" ){
                            window.location = result.forwardUrl;
                        }else{
                            console.log("Error: "+result.message);
                            $('#feedbackWindow').html('<div class="alert alert-danger" style="margin-top: 2em; margin-bottom: 2em;">'+result.message+'</div>');
                        }
                    }else{
                        console.log("Invalid response from server!");
                        $('#feedbackWindow').html('<div class="alert alert-danger" style="margin-top: 2em; margin-bottom: 2em;">The server had an unknown problem handling this TIP.  Please try again, or contact support.</div>');
                    }
                },
                error: function(){
                    console.log("Error!");
                }
            })
        }


        /**
         * Holds all of the TIP references that are active.
         */
        var TIP_REFERENCES = [];

        /**
         * Returns true if the given object is already being referenced.
         */
        function hasTipReference(ref){
            if( TIP_REFERENCES ){
                for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                    var cur = TIP_REFERENCES[i];
                    if( ref.Identifier === cur.Identifier ){
                        return true;
                    }
                }
            }
            return false;
        }//end hasTipReference()


        function doesRefIdExist(id){
            if( TIP_REFERENCES && TIP_REFERENCES.length > 0 ){
                for( var i = 0; i < TIP_REFERENCES.length; i++){
                    var ref = TIP_REFERENCES[i];
                    if( ref["$id"] === id ){
                        return true;
                    }
                }
            }
            return false;
        }
        function getNextTdReqId() {
            var counter = 1;
            while( doesRefIdExist("TD_"+counter) ){
                counter++;
            }
            return "TD_"+counter;
        }
        function getNextTipRefId() {
            var counter = 1;
            while( doesRefIdExist("TIP_"+counter) ){
                counter++;
            }
            return "TIP_"+counter;
        }

        /**
         * Called when the user selects a reference in the search dialog.
         */
        function addTipReferences(ref) {
            if( ref && ref.length ){
                for( var i = 0; i < ref.length; i++ ){
                    console.log("User selected: "+ref[i].Name+", v."+ref[i].Version);
                    if( ref[i].Type == "TrustmarkDefinition" ){
                        ref[i]['$id'] = getNextTdReqId();
                    }else{
                        ref[i]['$id'] = getNextTipRefId();
                    }
                    TIP_REFERENCES.push(ref[i]);
                }
            }else if( ref ) {
                console.log("User selected: "+ref.Name+", v."+ref.Version);
                if( ref[i].Type == "TrustmarkDefinition" ){
                    ref[i]['$id'] = getNextTdReqId();
                }else{
                    ref[i]['$id'] = getNextTipRefId();
                }
                TIP_REFERENCES.push(ref);
            }

            updateTipReferenceView();
        }//end addTipReferences()

        /**
         * Redraws the view of TIP references.
         */
        function updateTipReferenceView(){
            $('#referencesContainer').html('');
            console.log("Updating TIP Reference view...");

            $('#referencesContainer').append("<div>&nbsp;</div>");
            if( TIP_REFERENCES && TIP_REFERENCES.length > 0 ){
                for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                    console.log("Handling TIP reference #"+i);
                    var ref = TIP_REFERENCES[i];
                    var html = [];
                    html.push('<div class="row" style="margin-bottom: 1em;">');
                    html.push('  <div class="col-md-1" style="text-align: center;">');
                    html.push('    <span class="label label-default">'+ref['$id']+'</span>');
                    html.push('  </div>');
                    html.push('  <div class="col-md-11">');
                    html.push('    <div>');
                    html.push('       <div class="pull-right"><a href="javascript:removeReference('+i+')" class="btn btn-xs btn-danger">Remove</a></div>');
                    var typeIcon = 'th-list';
                    if( ref.Type === "TrustmarkDefinition" ){
                        typeIcon = 'tag';
                    }
                    html.push('      <div style="font-weight: bold;"><span class="glyphicon glyphicon-'+typeIcon+'"></span> '+ref.Name+', v'+ref.Version+'</div>');
                    html.push('      <div style="font-size: 90%; margin-left: 1.5em;" class="text-muted">'+ref.Description+'</div>');
                    html.push('    </div>');

                    if( ref.Type === "TrustmarkDefinition" ){
                        html.push('    <div>');
                        html.push('        <div class="pull-right">');
                        html.push('            <a href="javascript:addProvider('+ref.Identifier.hashCode()+')" class="btn btn-default btn-xs">Add Provider</a>');
                        html.push('        </div>');
                        html.push('    </div>\n ');
                        html.push('    <div id="providerContainer'+ref.Identifier.hashCode()+'" style="margin-left: 1.5em;">');
                        if( ref.Providers && ref.Providers.length > 0){
                            for( var j = 0; j < ref.Providers.length; j++ ){
                                html.push('<div>');
                                html.push('  Provider: ' + buildProviderSelect(ref.Providers[j]));
                                html.push(" <a class=\"removeProviderButton btn btn-default btn-xs\"><span class=\"glyphicon glyphicon-remove\"></span> Remove</a>");
                                html.push('</div>');
                            }
                        }
                        html.push('    </div>');
                    }

                    html.push('  </div>');
                    html.push('</div>');
                    if( i < (TIP_REFERENCES.length - 1) )
                        html.push("<hr />");
                    $('#referencesContainer').append(html.join("\n"));
                }
            }else{
                $('#referencesContainer').html('<p class="form-control-static"><em>There are no references.</em></p>');
            }

            $(".removeProviderButton").click(removeSelect);
        }//end updateTipReferenceView()

        function addProvider(hash){
            var html = "<div>Provider: "+buildProviderSelect(null)+" <a class=\"removeProviderButton btn btn-default btn-xs\"><span class=\"glyphicon glyphicon-remove\"></span> Remove</a></div>";
            $('#providerContainer'+hash).prepend(html);
            $(".removeProviderButton").click(removeSelect);
        }

        function removeSelect(){
            $(this).parent().remove();
        }

        function buildProviderSelect(provider){
            var html = [];

            html.push('<select class="providerSelect" onchange="javascript:parseProviders(TIP_REFERENCES);">');
            html.push('  <option value="">&nbsp;</option>');
            for( var i = 0; i < PROVIDERS.length; i++ ){
                var curProvider = PROVIDERS[i];
                if( provider && provider.Identifier === curProvider.Identifier ){
                    html.push('<option value="'+curProvider.Identifier+'" selected="selected">'+curProvider.Name+'</option>');
                }else{
                    html.push('<option value="'+curProvider.Identifier+'">'+curProvider.Name+'</option>');
                }
            }
            html.push('</select>');

            return html.join("\n");
        }

        function removeReference(index){
            var newReferences = [];
            for( var i = 0; i < TIP_REFERENCES.length; i++ ){
                if( i != index ){
                    newReferences.push(TIP_REFERENCES[i]);
                }
            }
            TIP_REFERENCES = newReferences;
            updateTipReferenceView();
        }

    </script>

</head>
<body>

<div id="page-body" role="main">

    <div class="row mainTitle">
        <div class="col-md-10">
            <h3 id="mainTitle">
                Trust Interoperability Profile Editor
            </h3>
            <div id="feedbackWindow">

            </div>
        </div>
        <div class="col-md-2" id="topRightContainer">
            <a href="javascript:saveTipToServer();" class="btn btn-primary">Save</a>
            <a href="${createLink(controller:'versionSetEdit', action:'index', id: vs.name)}" class="btn btn-default">Cancel</a>
        </div>
    </div>

    <div class="row tabsContainer">
        <div class="col-md-12">
            <div>

                <!-- Nav tabs -->
                <ul id="editorTabs" class="nav nav-tabs" role="tablist">
                    <li id="tabsMetadata" role="presentation"><a href="#metadataContainer" aria-controls="metadataContainer" role="tab" data-toggle="tab" id="metadataTab">Metadata</a></li>
                    <li id="tabsContent" role="presentation"><a href="#contentContainer" aria-controls="contentContainer" role="tab" data-toggle="tab" id="contentTab">Content</a></li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content" style="margin-top: 1em;">

                    <div role="tabpanel" class="tab-pane" id="metadataContainer">
                        <form class="form-horizontal">

                            <div class="form-group">
                                <label for="tipName" class="col-md-2 control-label">Name</label>
                                <div class="col-md-4">
                                    <input type="text" class="form-control" id="tipName" placeholder="">
                                </div>
                                <label for="tipPublicationDate" class="col-md-1 control-label" title="When this TIP is considered published.">Pub. Date</label>
                                <div class="col-md-2">
                                    <input type="text" class="form-control" id="tipPublicationDate" placeholder="2015-05-21">
                                </div>
                                <label for="tipVersion" class="col-md-1 control-label">Version</label>
                                <div class="col-md-2">
                                    <input type="text" class="form-control" id="tipVersion" placeholder="1.0-SNAPSHOT">
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="deprecatedButton" class="col-md-2 control-label">Deprecated</label>
                                <div class="col-md-2">
                                    <a href="javascript:toggleDeprecated()" id="deprecatedButton" class="btn btn-default">
                                        <span id="deprecatedIndicator" class="glyphicon glyphicon-remove"></span>
                                        <span id="deprecatedText">Not Deprecated</span>
                                    </a>
                                </div>

                                <div id="supersedByContainer">
                                    <label for="supersededBy" class="col-md-2 control-label">Superseded By</label>
                                    <div class="col-md-6">
                                        <textarea class="form-control" id="supersededBy" name="supersededBy"></textarea>
                                    </div>
                                </div>
                            </div>


                            <div class="form-group">
                                <label for="identifier" class="col-md-2 control-label">Identifier</label>
                                <div class="col-md-10">
                                    <input type="text" class="form-control" id="identifier" placeholder="https://...">
                                </div>
                            </div>



                            <div class="form-group">
                                <label for="tipIssuerName" class="col-md-2 control-label">Issuer Organization</label>
                                <div class="col-md-10">
                                    <div class="form-group">
                                        <label for="tipIssuerName" class="col-md-2 control-label">Name</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerName" type="text" class="form-control" placeholder="Name" />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tipIssuerId" class="col-md-2 control-label">URI</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerId" type="text" class="form-control" placeholder="http://www..." />
                                        </div>
                                    </div>

                                    <hr />

                                    <div class="form-group">
                                        <label for="tipIssuerResponder" class="col-md-2 control-label">Responder</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerResponder" type="text" class="form-control" placeholder="Name" />
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="tipIssuerEmail" class="col-md-2 control-label">Email</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerEmail" type="text" class="form-control" placeholder="user@domain.com" />
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="tipIssuerPhone" class="col-md-2 control-label">Phone Number</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerPhone" type="text" class="form-control" placeholder="xxx-xxx-xxxx" />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tipIssuerMailingAddr" class="col-md-2 control-label">Address</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerMailingAddr" type="text" class="form-control" placeholder="123 Main St..." />
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="tipIssuerNotes" class="col-md-2 control-label">Notes</label>
                                        <div class="col-md-10">
                                            <input id="tipIssuerNotes" type="text" class="form-control" placeholder="..." />
                                        </div>
                                    </div>

                                </div>
                            </div>


                            <div class="form-group">
                                <label for="tipDescription" class="col-md-2 control-label">Description</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="tipDescription" name="tipDescription"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-md-2 control-label">Keywords</label>
                                <div class="col-md-10">
                                    <div class="form-control-static" id="keywordsContainer">
                                        Rendering keywords...
                                    </div>
                                    <div class="keywordAddButtonContainer">
                                        <a href="javascript:addNewKeyword()" class="btn btn-primary" title="Add a new keyword.">
                                            <span class="glyphicon glyphicon-plus"></span>
                                            Add
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="Supersedes" class="col-md-2 control-label">Supersedes</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="Supersedes" name="Supersedes"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="LegalNotice" class="col-md-2 control-label">Legal Notice</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="LegalNotice" name="LegalNotice"></textarea>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="Notes" class="col-md-2 control-label">Notes</label>
                                <div class="col-md-10">
                                    <textarea class="form-control" id="Notes" name="Notes"></textarea>
                                </div>
                            </div>

                        </form>
                    </div>

                    <div role="tabpanel" class="tab-pane" id="contentContainer">
                        <div class="text-muted" style="margin-bottom: 0.5em;">Here you can edit the content of this TIP, which is the TD and TIP references and the Trust Expression.</div>

                        <form>
                            <div class="form-group">
                                <label for="trustExpression">Trust Expression</label>
                                <textarea id="trustExpression" name="trustExpression" class="form-control"></textarea>
                            </div>
                        </form>

                        <div style="margin-top: 2em;" class="row">
                            <div class="col-md-12">
                                <h3 style="margin: 0; padding: 0;">References <span id="referenceCount"></span></h3>
                                <hr style="margin: 0; padding: 0;" />
                                <div id="referencesContainer">
                                    Loading...
                                </div>
                                <div style="margin-top: 1em;" id="tipReferenceButtons">&nbsp;</div>
                            </div>
                        </div>

                    </div>

                </div>

            </div>
        </div>
    </div>

    <tmpl:/templates/tipReferenceSearch buttonContainerId="tipReferenceButtons" onTipReferenceAddFunction="addTipReferences" hasTipReferenceFunction="hasTipReference" />

    <sec:authorize access="hasAuthority('tpat-admin')">
        <div style="margin-top: 4em;">
            <a href="javascript:menuAutofillTestData()" class="btn btn-warning">Auto-Fill</a>
            <a onclick="menuFileDownloadJSON(); return false;" class="btn btn-warning">Show JSON</a>
        </div>
        <div id="jsonView">&nbsp;</div>
    </sec:authorize>


</div><!-- /.container -->

<div style="height: 5em;">&nbsp;</div>


</body>
</html>

