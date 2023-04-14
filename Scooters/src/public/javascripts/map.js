import scooters from '/scooters.json' assert { type: 'json' }
import warehouses from '/warehouses.json' assert { type: 'json' }
import unloading_areas from '/unloading_area.json' assert { type: 'json' }
let scooter_marks = []
let warehouses_marks = []
let unloading_area_marks = []

function init(){
    let map = new ymaps.Map('ya-map', {
        center: [59.985444887334296,30.30097846294469],
        zoom: 14,
        controls: []
    })
    console.log(scooters)
    //Метки самокатов
    for (let i = 0; i < scooters.length; i++){
        scooter_marks.push(new ymaps.Placemark([scooters[i].coordinate_x, scooters[i].coordinate_y], {
            balloonContentHeader: 'Самокат №' + scooters[i].id,
            balloonContentBody: 'Процент заряда: ' + scooters[i].battery + '%',
            balloonContentFooter: 'Координаты: x=' + scooters[i].coordinate_x + ', y=' + scooters[i].coordinate_y,
        }, {
            iconLayout: 'default#image',
            iconImageHref: 'https://cdn-icons-png.flaticon.com/512/1825/1825782.png',
            iconImageSize: [40, 40],
            iconImageOffset: [-19, -44]
        }));
    }

    //Метки складов
    for (let i = 0; i < warehouses.length; i++){
        warehouses_marks.push(new ymaps.Placemark([warehouses[i].coordinate_x, warehouses[i].coordinate_y], {
            balloonContentHeader: 'Склад №' + warehouses[i].id,
            balloonContentBody: 'Текущее кол-во самокатов: ' + warehouses[i].numberOfScooters + '<br>' +
                                'Максимальное кол-во самокатов: ' + warehouses[i].maxNumberOfScooters,
            balloonContentFooter: 'Координаты: x=' + warehouses[i].coordinate_x + ', y=' + warehouses[i].coordinate_y,
        }, {
            iconLayout: 'default#image',
            iconImageHref: 'https://cdn-icons-png.flaticon.com/512/2066/2066573.png',
            iconImageSize: [40, 40],
            iconImageOffset: [-19, -44]
        }));
    }

    //Метки площадок выгрузки
    for (let i = 0; i < unloading_areas.length; i++){
        unloading_area_marks.push(new ymaps.Placemark([unloading_areas[i].coordinate_x, unloading_areas[i].coordinate_y], {
            balloonContentHeader: 'Площадка выгрузки №' + unloading_areas[i].id,
            balloonContentBody: 'Адресс: ' + unloading_areas[i].address,
            balloonContentFooter: 'Координаты: x=' + unloading_areas[i].coordinate_x + ', y=' + unloading_areas[i].coordinate_y,
        }, {
            iconLayout: 'default#image',
            iconImageHref: 'https://cdn-icons-png.flaticon.com/512/2850/2850710.png',
            iconImageSize: [40, 40],
            iconImageOffset: [-19, -44]
        }));
    }

    //Добавление меток
    //Самокаты
    for (let i = 0; i < scooter_marks.length; i++)
        map.geoObjects.add(scooter_marks[i]);

    //Склады
    for (let i = 0; i < warehouses_marks.length; i++)
        map.geoObjects.add(warehouses_marks[i]);

    //Выгрузки
    for (let i = 0; i < unloading_area_marks.length; i++)
        map.geoObjects.add(unloading_area_marks[i]);

    //map.controls.remove('geolocationControl'); // удаляем геолокацию
    //map.controls.remove('searchControl'); // удаляем поиск
    //map.controls.remove('trafficControl'); // удаляем контроль трафика
    //map.controls.remove('typeSelector'); // удаляем тип
    //map.controls.remove('fullscreenControl'); // удаляем кнопку перехода в полноэкранный режим
    //map.controls.remove('zoomControl'); // удаляем контрол зуммирования
    //map.controls.remove('rulerControl'); // удаляем контрол правил
}

ymaps.ready(init)