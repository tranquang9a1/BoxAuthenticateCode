# BoxAuthenticateCode
Manual get authenticate code from box.com Java

Create class ApiClientConfig to using this function

<code>ApiClientConfig config = new ApiClientConfig(HOST,USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);</code>

After that call function

<code>BoxApiClient client = new BoxApiClient();</code>
<code>String authorizeCode = client.getAuthorizeCode(config);</code>


After that using BoxSDK (https://github.com/box/box-java-sdk) to authorize
