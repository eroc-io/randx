//检查浏览器是否支持WebSocket
if (window.WebSocket) {
    console.log('This browser supports WebSocket');
} else {
    console.log('This browser does not supports WebSocket');
    alert('This browser does not supports WebSocket');
}
//.proto文件路径
const protoUrl = "/js/comms.proto";

// const webSocketPath = "ws://192.168.10.153:8080/ws";
const webSocketPath = "ws://localhost:8080/ws";

const CARD_INDEX = 7;
//大厅座位使用信息
var hallMessages = [];
//桌号
var deckNo = null;
//Uint8Array，桌编号
var deckId = null;
//自己的座位号信息
var myNumber = null;
//空桌信息
var emptySeat = null;
//Uint8Array，盐信息
var salt = null;
//[] Uint8Array，所持牌的所有盐信息
var salts = [];
//所持牌的储存对象
var saltsObj = {};
//Uint8Array，服务器公钥
var dpk = null;
//Uint8Array，牌证明
var proofs = {};
//Uint8Array
var cards = [];
//其他玩家的公钥
var pks = [];
//有次序的玩家的公钥
var orderPks = {};

var ws = new WebSocket(webSocketPath);
ws.binaryType = "arraybuffer";

ws.onopen = function (evt) {

    console.log("Connection open ...");

};

ws.onmessage = async function getMessage(evt) {

    let buffer = evt.data;
    let [num, proBuffer] = subByte(buffer);

    switch (num) {
        case 0:
            //开局返回的deckId，responseId = 0
            let obj = await readPbf(protoUrl, "OpenResponse", proBuffer);
            deckId = obj.deckId;
            console.log(obj.errMsg);
            break;
        case 1:
            //加入牌局返回的salt和服务器pk，responseId = 1
            let obj1 = await readPbf(protoUrl, "StartResponse", proBuffer);
            salt = obj1.salt;
            dpk = obj1.dpk;
            console.log(obj1.errMsg);
            break;
        case 2:
            //抓牌返回的salt，取得牌信息储存，responseId = 2
            let obj2 = await readPbf(protoUrl, "DrawResponse", proBuffer);
            console.log(obj2.errMsg);
            if (!obj2.errMsg) {
                let cardCipher = obj2.cardCipher;
                salt = new Uint8Array(await decryptByECIES(cardCipher));
                salts.push(salt);
                cards.push(salt[CARD_INDEX]);
                saltsObj[btoa(uint8ArrayToString(salt))] = salt;

                if (salt[CARD_INDEX]) {

                    showOneCard("player" + myNumber, salt);
                }
                await drawCard();
            }

            if (obj2.errMsg == "抽牌结束") {

                document.getElementById("drawLeft" + deckNo).removeAttribute("disabled");
            }
            break;
        case 3:
            //其他玩家抓到牌的proof， responseId = 3
            let obj3 = await readPbf(protoUrl, "DrawNotification", proBuffer);
            let pk = obj3.pk;
            let pkBase64 = btoa(uint8ArrayToString(pk));

            if (pks.indexOf(pkBase64) == -1) {
                pks.push(pkBase64);
                proofs[pkBase64] = [];
            }
            proofs[pkBase64].push(btoa(uint8ArrayToString(obj3.proof)));

            for (let num in orderPks) {

                if (btoa(uint8ArrayToString(orderPks[num])) == btoa(uint8ArrayToString(pk))) {

                    createOtherCard("player" + num, btoa(uint8ArrayToString(obj3.proof)));
                }
            }
            break;
        case 4:
            //查看底牌返回的牌信息，取得牌信息储存，responseId = 4
            let obj4 = await readPbf(protoUrl, "DrawLeftNotification", proBuffer);
            let reCards = [];
            for (let i of obj4.cards) {

                reCards.push(cardNames[i]);
            }
            showMessage("p3", "底牌:" + reCards);
            salt = obj4.salt;
            console.log(obj4.errMsg);
            break;
        case 5:
            //还牌信息, responseId = 5
            let obj5 = await readPbf(protoUrl, "ReturnResponse", proBuffer);
            let numReturned = obj5.numReturned;
            showMessage("p3", '您还了 ' + numReturned + ' 张牌' + backCard);
            document.getElementById("returnCards" + deckNo).style.display = "none";
            salt = obj5.salt;
            console.log(obj5.errMsg);
            break;
        case 6:
            //其他玩家还牌信息, responseId = 6
            let obj6 = await readPbf(protoUrl, "ReturnNotification", proBuffer);

            for (let num in orderPks) {

                if (btoa(uint8ArrayToString(orderPks[num])) == btoa(uint8ArrayToString(obj6.pk))) {

                    showMessage("p3", 'player' + num + '还了' + obj6.numReturned + ' 张牌' + backCard);
                    document.getElementById("returnCards" + deckNo).style.display = "none";
                }
            }

            break;
        case 7:
            //加入牌局返回的自己编号和空桌信息，responseId = 7
            let obj7 = await readPbf(protoUrl, "JoinResponse", proBuffer);
            myNumber = obj7.number;
            emptySeat = obj7.emptySeat;
            let joinNotifys = obj7.joinNotify;

            if (joinNotifys) {
                for (let join of joinNotifys) {
                    orderPks[join.joinSeat] = join.joinpk;
                }
            }
            orderPks[myNumber] = new Uint8Array(pkBuffer);

            showMessage("p1", '分配的座位号：' + myNumber);

            if (emptySeat) {
                showMessage("p2", '还缺' + emptySeat + '个人可开始游戏');

            } else {
                //开始按钮可选，可以开始游戏了
                document.getElementById("draw" + deckNo).removeAttribute("disabled");
                showMessage("p2", '玩家全部准备完毕，可以开始游戏了');

            }

            break;
        case 8:
            //其他玩家加入牌局返回的空桌信息，其他玩家编号和其他玩家pk，responseId = 8
            let obj8 = await readPbf(protoUrl, "JoinNotification", proBuffer);
            emptySeat = obj8.emptySeat;
            orderPks[obj8.joinSeat] = obj8.joinpk;
            showMessage("p1", obj8.joinSeat + '号座位加入了');

            if (emptySeat) {
                showMessage("p2", '还缺' + emptySeat + '个人可开始游戏');

            } else {
                showMessage("p2", '玩家全部准备完毕，可以开始游戏了');
                //开始按钮可选，可以开始游戏了
                document.getElementById("draw" + deckNo).removeAttribute("disabled");
            }

            break;

        case 9:
            //玩家进入大厅返回的大厅信息，responseId = 9
            let obj9 = await readPbf(protoUrl, "HallResponse", proBuffer);
            hallMessages = obj9.deck;

            if (hallMessages) {

                for (let hallMessage of hallMessages) {

                    console.log(hallMessage.emptyNum);
                    console.log(hallMessage.deckNo + '号桌还有' + hallMessage.emptyNum + '个空位');
                    let seat = hallMessage.seat;
                    let msg = '';
                    if (seat) {
                        for (let i of Array.from(seat)) {
                            msg += i + ' ';
                        }
                        console.log(msg + '号座位已有人，不能选择');
                    } else {
                        console.log('，此桌目前还无人');
                    }
                }
            } else {
                console.log('请选择牌桌')
            }

            break;

        case 10:
            //返回其他玩家出牌信息和其他玩家pk，responseId = 10
            let obj10 = await readPbf(protoUrl, "DisCardsNotify", proBuffer);
            let outSalts = sliceUint8Array(obj10.salt, 32);
            let outSaltBase64;
            for (let outSalt of outSalts) {

                outSaltBase64 = btoa(uint8ArrayToString(new Uint8Array(await sha256(outSalt))));

                if (proofs[btoa(uint8ArrayToString(obj10.pk))].indexOf(outSaltBase64) < 0) {

                    throw new Error('Illegal card');
                }

                showOtherCard(outSaltBase64, cardNames[outSalt[CARD_INDEX]]);
            }

            break;
    }
};


ws.onerror = function (evt) {
    onError(evt)
};

ws.onclose = function (evt) {

    console.log("Connection closed.");
    console.log(evt.code);
    console.log(evt.reason);
};


//open游戏
async function openGame(deckNum, cards, decks, players, rounds) {
    deckNo = deckNum;

    let payload = {deckNo: deckNo, numCards: cards, numDecks: decks, numPlayers: players, rounds: rounds};

    let buffer = await createPbf(protoUrl, "OpenRequest", payload);

    ws.send(addOneByte(0, buffer));
};

//加入游戏 funcId = 1
async function joinGame() {

    await createKey();

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer)};

    let buffer = await createPbf(protoUrl, "JoinRequest", payload);

    ws.send(addOneByte(1, buffer));
};


//抓牌 funcId = 2
async function drawCard() {

    let sign = await signByECDSA(salt);

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

    let buffer = await createPbf(protoUrl, "DrawRequest", payload);

    ws.send(addOneByte(2, buffer));

};

//剩余牌 funcId = 3
async function drawLeftCards() {

    let allPk = [];

    for (let i = 1; i <= Object.keys(orderPks).length; i++) {
        //合并所有人的pk
        allPk.push(orderPks[i]);

    }

    let sign = await signByECDSA(await sha256(concatUint8Array(allPk)));

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

    let buffer = await createPbf(protoUrl, "DrawLeftRequest", payload);

    ws.send(addOneByte(3, buffer));
};


//还牌 funcId = 4
async function returnCards() {

    let checkVal = removeCard("player" + myNumber);

    let sign = await signByECDSA(salt);

    let macObj = await encryptByECIES(dpk, concatUint8Array(checkVal));

    let payload = {
        deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign),
        cardsCipher: macObj
    };

    let buffer = await createPbf(protoUrl, "ReturnRequest", payload);

    ws.send(addOneByte(4, buffer));
};

//亮牌 funcId = 5
async function outCards() {

    let checkVal = removeCard("player" + myNumber);

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), salt: concatUint8Array(checkVal)};

    let buffer = await createPbf(protoUrl, "DisCard", payload);

    ws.send(addOneByte(5, buffer));

}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
};