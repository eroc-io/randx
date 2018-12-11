const cardNames = [
    '2♢', '3♢', '4♢', '5♢', '6♢', '7♢', '8♢', '9♢', '10♢', 'J♢', 'Q♢', 'K♢', 'A♢',
    '2♧', '3♧', '4♧', '5♧', '6♧', '7♧', '8♧', '9♧', '10♧', 'J♧', 'Q♧', 'K♧', 'A♧',
    '2♡', '3♡', '4♡', '5♡', '6♡', '7♡', '8♡', '9♡', '10♡', 'J♡', 'Q♡', 'K♡', 'A♡',
    '2♤', '3♤', '4♤', '5♤', '6♤', '7♤', '8♤', '9♤', '10♤', 'J♤', 'Q♤', 'K♤', 'A♤',
    'b🃏', 'c🃏'
];

const backCard = '█';


var checkBoxValueList = [];
var checkBoxList = [];


window.onload = function () {
    document.getElementById("open1").addEventListener("click", function () {
        changeCancel("open1", "join1");
        nonoCancel(["cardTable2", "cardTable3"])
    });
    document.getElementById("join1").addEventListener("click", function () {
        changeCancel("join1", "draw1");
    });
    document.getElementById("draw1").addEventListener("click", function () {
        changeCancel("draw1", "drawLeft1");
    });
    document.getElementById("drawLeft1").addEventListener("click", function () {
        changeCancel("drawLeft1", "returnCards1");
    });
}

function changeCancel(noneStr, blockStr) {
    document.getElementById(blockStr).style.display = "block";
    document.getElementById(noneStr).style.display = "none";
}

function nonoCancel(noneStrs) {
    for (let noneSt of noneStrs) {
        document.getElementById(noneSt).style.display = "none";
    }
}


function showMessage(divId, showMes) {

    document.getElementById(divId).innerText = showMes;
}


//展示手牌
function showCardFunction(seatDivId, salt) {

    let generateDiv = document.getElementById(seatDivId);
    //创建div，如果存在就不创建
    if (!generateDiv) {

        generateDiv = document.createElement("div");
        generateDiv.id = seatDivId;
        generateDiv.addEventListener("click", eventListenerFunction, false);
        document.body.appendChild(generateDiv);

        let p = document.createElement('p');
        p.innerHTML = 'me(' + seatDivId + ")";
        generateDiv.appendChild(p);
    }

    // 加入复选框
    let checkBox = document.createElement("input");
    checkBox.setAttribute("type", "checkbox");
    let saltBase64 = window.btoa(uint8ArrayToString(salt));
    checkBox.setAttribute("id", saltBase64);
    checkBox.setAttribute("value", saltBase64);
    generateDiv.appendChild(checkBox);

    let span = document.createElement('span');
    span.innerHTML = cardNames[salt[CARD_INDEX]];
    generateDiv.appendChild(span);
    generateDiv.appendChild(document.createTextNode(" "));

}

//checkBox选中处理
function eventListenerFunction(event) {

    let target = event.target;

    //选中加入数组里
    if (target.checked) {

        checkBoxValueList.push(target.value);
        checkBoxList.push(target);

        //未选中从数组里去除
    } else {
        let index = checkBoxValueList.indexOf(target.value);
        checkBoxValueList.splice(index, 1);
        checkBoxList.splice(index, 1);

    }

}


//生成其他人手牌，不展示具体牌
function createOtherCard(seatDivId, proof) {

    let generateDiv = document.getElementById(seatDivId);
    //创建div，如果存在就不创建
    if (!generateDiv) {

        generateDiv = document.createElement("div");
        generateDiv.id = seatDivId;
        document.body.appendChild(generateDiv);

        let p = document.createElement('p');
        p.innerHTML = seatDivId;
        generateDiv.appendChild(p);
    }

    let span = document.createElement('span');
    span.setAttribute("id", proof);
    span.innerHTML = backCard;
    generateDiv.appendChild(span);
    generateDiv.appendChild(document.createTextNode(" "));
}

//展示其他人亮出来的手牌
function showOtherCard(spanId, card) {

    let span = document.getElementById(spanId);
    span.innerHTML = card;

}


//去掉自己还回去和亮出来的手牌
function removeCard(seatDivId) {

    //获取复选框选择的牌的salt
    let checkVal = [];
    for (let checkBoxValue of checkBoxValueList) {

        checkVal.push(saltsObj[checkBoxValue]);

    }
    checkBoxValueList = [];

    let generateDiv = document.getElementById(seatDivId);

    for (let checkBox of checkBoxList) {

        generateDiv.removeChild(checkBox);
        // checkBox.parentNode.removeChild(checkBox);
    }

    checkBoxList = [];

    return checkVal;

}



