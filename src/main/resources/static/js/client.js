/**
 * Created by stephan on 20.03.16.
 */

$(function () {
    // VARIABLES =============================================================
    var TOKEN_KEY = "jwtToken"
    var access_key = "wxf183e81ce4464169";
    var $notLoggedIn = $("#notLoggedIn");
    var $loggedIn = $("#loggedIn").hide();
    var $response = $("#response");
    var $login = $("#login");
    var $userInfo = $("#userInfo").hide();

    // FUNCTIONS =============================================================
    function getJwtToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setJwtToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function removeJwtToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    function doLogin() {
        var code = getQueryString("code");
        if(code){
            $.ajax({
                url: "/werewolf/auth",
                type: "POST",
                data: JSON.stringify({code:code}),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (data, textStatus, jqXHR) {
                    setJwtToken(data.token);
                    $login.hide();
                    $notLoggedIn.hide();
                    showTokenInformation()
                    showUserInformation();
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status === 401) {
                        $('#loginErrorModal')
                            .modal("show")
                            .find(".modal-body")
                            .empty()
                            .html("<p>Spring exception:<br>" + jqXHR.responseJSON.exception + "</p>");
                    } else {
                        throw new Error("an unexpected error occured: " + errorThrown);
                    }
                }
            });
        }else{
            location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+access_key+"&redirect_uri="+encodeURIComponent(location.href)+"&response_type=code&scope=snsapi_userinfo#wechat_redirect";
        }

    }

    function doLogout() {
        removeJwtToken();
        $login.show();
        $userInfo
            .hide()
            .find("#userInfoBody").empty();
        $loggedIn
            .hide()
            .attr("title", "")
            .empty();
        $notLoggedIn.show();
    }

    function createAuthorizationTokenHeader() {
        var token = getJwtToken();
        if (token) {
            return {"Authorization": token};
        } else {
            return {};
        }
    }

    function showUserInformation() {
        $.ajax({
            url: "/werewolf/user",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            headers: createAuthorizationTokenHeader(),
            success: function (data, textStatus, jqXHR) {
                var $userInfoBody = $userInfo.find("#userInfoBody");

                $userInfoBody.append($("<div>").text("Username: " + data.username));
                $userInfoBody.append($("<div>").text("Email: " + data.email));

                var $authorityList = $("<ul>");
                data.authorities.forEach(function (authorityItem) {
                    $authorityList.append($("<li>").text(authorityItem.authority));
                });
                var $authorities = $("<div>").text("Authorities:");
                $authorities.append($authorityList);

                $userInfoBody.append($authorities);
                $userInfo.show();
            }
        });
    }

    function showTokenInformation() {
        $loggedIn
            .text("Token: " + getJwtToken())
            .attr("title", "Token: " + getJwtToken())
            .show();
    }

    function showResponse(statusCode, message) {
        $response
            .empty()
            .text("status code: " + statusCode + "\n-------------------------\n" + message);
    }

    // REGISTER EVENT LISTENERS =============================================================
    $("#loginForm").submit(function (event) {
        event.preventDefault();

        var $form = $(this);
        var formData = {
            username: $form.find('input[name="username"]').val(),
            password: $form.find('input[name="password"]').val()
        };

        doLogin(formData);
    });

    $("#logoutButton").click(doLogout);

    $("#exampleServiceBtn").click(function () {
        $.ajax({
            url: "/werewolf/persons",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            headers: createAuthorizationTokenHeader(),
            success: function (data, textStatus, jqXHR) {
                showResponse(jqXHR.status, JSON.stringify(data));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showResponse(jqXHR.status, errorThrown);
            }
        });
    });

    $("#adminServiceBtn").click(function () {
        $.ajax({
            url: "/werewolf/protected",
            type: "GET",
            contentType: "application/json; charset=utf-8",
            headers: createAuthorizationTokenHeader(),
            success: function (data, textStatus, jqXHR) {
                showResponse(jqXHR.status, data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showResponse(jqXHR.status, errorThrown);
            }
        });
    });

    $loggedIn.click(function () {
        $loggedIn
                .toggleClass("text-hidden")
                .toggleClass("text-shown");
    });

    // INITIAL CALLS =============================================================
    if (getJwtToken()) {
        $login.hide();
        $notLoggedIn.hide();
        showTokenInformation();
        showUserInformation();
    }else{
        doLogin();
    }
});