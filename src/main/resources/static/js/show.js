const cardNames = [
    '2♢', '3♢', '4♢', '5♢', '6♢', '7♢', '8♢', '9♢', '10♢', 'J♢', 'Q♢', 'K♢', 'A♢',
    '2♧', '3♧', '4♧', '5♧', '6♧', '7♧', '8♧', '9♧', '10♧', 'J♧', 'Q♧', 'K♧', 'A♧',
    '2♡', '3♡', '4♡', '5♡', '6♡', '7♡', '8♡', '9♡', '10♡', 'J♡', 'Q♡', 'K♡', 'A♡',
    '2♤', '3♤', '4♤', '5♤', '6♤', '7♤', '8♤', '9♤', '10♤', 'J♤', 'Q♤', 'K♤', 'A♤',
    'b🃏', 'c🃏'
];

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
function showFunction(salt) {

    var executerDiv = document.getElementById("cards");
    let showCard = salt[CARD_INDEX];

// 加入复选框
    var checkBox = document.createElement("input");
    checkBox.setAttribute("type", "checkbox");
    checkBox.setAttribute("name", "card");
    checkBox.setAttribute("value", window.btoa(uint8ArrayToString(salt)));


    executerDiv.appendChild(checkBox);
    executerDiv.appendChild(document.createTextNode(cardNames[showCard]));
    executerDiv.appendChild(document.createTextNode("         "));

}

