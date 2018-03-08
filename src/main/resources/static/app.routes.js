angular.module('cts')
    .config(function($routeProvider, $locationProvider) {
        $routeProvider
            .when("/", {
                templateUrl: "components/single/singleView.html"
            })
            .when("/batch", {
                templateUrl: "components/batch/batchView.html"
            })
            .when("/services", {
                templateUrl: "components/services/servicesView.html"
            });

        $locationProvider.html5Mode(true);
        $locationProvider.hashPrefix("");
    }).run(['$anchorScroll', function($anchorScroll) {
        $anchorScroll.yOffset = 60;
    }]).run(['$rootScope', function($rootScope) {
        $rootScope.APP_NAME = 'The Chemical Translation Service';
        $rootScope.APP_NAME_ABBR = 'CTS';
        $rootScope.APP_VERSION = 'v1.0';
    }]);