/**
 * Created by matthewmueller on 3/16/17.
 */

angular.module('cts', ['ngRoute', 'ngBreakpoint', 'angularFileUpload', 'btorfs.multiselect', 'ui.bootstrap'])
    .config(function($routeProvider, $locationProvider) {
        $routeProvider
            .when("/", {
                templateUrl: "views/main.htm"
            })
            .when("/batch", {
                templateUrl: "views/batch.htm"
            })
            .when("/services", {
                templateUrl: "views/services.htm"
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

