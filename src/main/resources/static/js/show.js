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

//按钮事件监听，状态修改
window.onload = function () {
    document.getElementById("open1").addEventListener("click", function () {
        changeCancel("open1", "join1");
        nonoCancel(["cardTable2", "cardTable3"])
    });
    document.getElementById("join1").addEventListener("click", function () {
        changeCancel("join1", "draw1");
    });
    document.getElementById("draw1").addEventListener("click", function () {
        changeCancel("draw1", "lookLeft1");
    });
    document.getElementById("lookLeft1").addEventListener("click", function () {
        changeCancel("lookLeft1", "drawLeft1");
    });
    document.getElementById("drawLeft1").addEventListener("click", function () {
        changeCancel("drawLeft1", "returnCards1");
    });


    document.getElementById("open2").addEventListener("click", function () {
        changeCancel("open2", "join2");
        nonoCancel(["cardTable1", "cardTable3"])
    });
    document.getElementById("join2").addEventListener("click", function () {
        changeCancel("join2", "draw2");
    });
    document.getElementById("draw2").addEventListener("click", function () {
        changeCancel("draw2", "lookLeft2");
    });
    document.getElementById("lookLeft2").addEventListener("click", function () {
        changeCancel("lookLeft2", "drawLeft2");
    });
    document.getElementById("drawLeft2").addEventListener("click", function () {
        changeCancel("drawLeft2", "returnCards2");
    });


    document.getElementById("open3").addEventListener("click", function () {
        changeCancel("open3", "join3");
        nonoCancel(["cardTable1", "cardTable2"])
    });
    document.getElementById("join3").addEventListener("click", function () {
        changeCancel("join3", "draw3");
    });
    document.getElementById("draw3").addEventListener("click", function () {
        changeCancel("draw3", "lookLeft3");
    });
    document.getElementById("lookLeft3").addEventListener("click", function () {
        changeCancel("lookLeft3", "drawLeft3");
    });
    document.getElementById("drawLeft3").addEventListener("click", function () {
        changeCancel("drawLeft3", "returnCards3");
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
function showOneCard(seatDivId, salt) {

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

        if (target.type == "checkbox") {
            let index = checkBoxValueList.indexOf(target.value);
            checkBoxValueList.splice(index, 1);
            checkBoxList.splice(index, 1);
        }
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


//复选框选择的手牌
function checkCard() {

    //获取复选框选择的牌的salt
    let checkVal = [];
    for (let checkBoxValue of checkBoxValueList) {

        checkVal.push(saltsObj[checkBoxValue]);

    }

    return checkVal;

}


//去掉自己还回去和亮出来的手牌
function removeCard(seatDivId) {

    checkBoxValueList = [];

    let generateDiv = document.getElementById(seatDivId);

    for (let checkBox of checkBoxList) {

        generateDiv.removeChild(checkBox);
        // checkBox.parentNode.removeChild(checkBox);
    }

    checkBoxList = [];

}
