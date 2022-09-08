<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>

    <title>${grailsApplication.config.tf.org.toolheader} | Login</title>

    <style type="text/css">

    </style>

</head>
<body>
<div id="page-body" role="main">

    <div class="row">
        <div class="col-md-6">
            <g:if test='${flash.message}'>
                <div style="margin-top: 1em; margin-bottom: 1em;" class="alert alert-danger">${flash.message}</div>
            </g:if>

            <h3>Please Login</h3>

            <form action="${postUrl ?: '/login/authenticate'}" method="POST" id="loginForm" class="form form-horizontal" autocomplete="off">

                <div class="form-group">
                    <label for="username" class="col-sm-2 control-label">Email</label>
                    <div class="col-sm-10">
                        <input type="text" class="form-control" name="${usernameParameter ?: 'username'}" id="username" placeholder="address@domain.ext"/>
                    </div>
                </div>

                <div class="form-group">
                    <label for="password" class="col-sm-2 control-label">Password</label>
                    <div class="col-sm-10">
                        <input type="password" class="form-control" name="${passwordParameter ?: 'password'}" id="password"/>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" class="chk" name="${rememberMeParameter ?: 'remember-me'}" id="remember_me" <g:if test='${hasCookie}'>checked="checked"</g:if>/>
                                <g:message code='springSecurity.login.remember.me.label'/>
                            </label>
                        </div>
                    </div>
                </div>


                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <input class="btn btn-primary" type="submit" id="submit" value="${message(code: 'springSecurity.login.button')}"/>
                    </div>
                </div>

            </form>

        </div>

        <div class="col-md-6">
            <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
                <h3>Auto Login</h3>
                <ul style="margin: 0; padding: 0; list-style: none;">
                    <%
                        ResourceBundle defaultAccountsBundle = ResourceBundle.getBundle("defaultAccounts");
                        int userCount = Integer.parseInt(defaultAccountsBundle.getString("user.count"));
                        for( int i = 0; i < userCount; i++ ){
                            int userIndex = i+1;
                            String username = defaultAccountsBundle.getString("user."+userIndex+".username");
                            String password = defaultAccountsBundle.getString("user."+userIndex+".password");
                            String name = defaultAccountsBundle.getString("user."+userIndex+".name");

                            %>
                    <li style="margin-bottom: 1em;">
                        <a href="javascript:autofillUserInfo('<%= username %>', '<%= password %>')" class="btn btn-default">
                            <%= name %>, <%= username %>
                        </a>
                    </li>

                    <%
                        }
                    %>
                </ul>

                <script type="text/javascript">
                    function autofillUserInfo(username, password){
                        $('#username').val(username);
                        $('#password').val(password);
                        $('#submit').click();
                    }
                </script>
            </g:if>
        </div>

    </div>


</div>
</body>
</html>
