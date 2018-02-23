(function() {
    'use strict';

    angular.module('cts')
        .directive('navBar', navBar);

    function navBar() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/navigation/navBarView.html'
        };
    }
})();