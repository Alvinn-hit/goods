(function () {
    'use strict';

    angular
        .module('custom', [
            'angular-ladda',
            'blockUI',
            'ngTable',
            'angucomplete-alt'
        ])
        .config(
            function (blockUIConfig) {
                blockUIConfig.autoBlock = false;
                blockUIConfig.message = "正在加载..."
            })
        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push(["$q", "$rootScope", function ($q, $rootScope) {
                return {
                    'response': function (response) {
                        var data = response.data;
                        if (data != undefined && data.status != undefined && data.status != 200) {
                            $rootScope.$broadcast("TOAST", {msg: data.data, status: 'danger'});
                        }
                        return response;
                    }
                };
            }])
        }])
        .run(['NotifyService', '$rootScope',
            function (NotifyService, $rootScope) {
                if (window.__current != 0) {
                    $rootScope.userId = window.__current;
                    $rootScope.orgId = window.__orgId;
                    $rootScope.orgName = window.__orgName;
                    $rootScope.name = window.__name;
                    NotifyService.initialize();
                }
            }]
        );
})();