(function() {
    'use strict';

    angular.module('cts')
        .directive('shortcutBox', shortcutBox);

    function shortcutBox() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/shared/sidebar/sidebarView.html'
        };
    }

})();