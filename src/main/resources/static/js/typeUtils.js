//ArrayBuffer转hexString
function buf2HexString(buf) {
    return Array.from(new Uint8Array(buf))
        .map(x => ('00' + x.toString(16)).slice(-2))
        .join('')
}

//hexString转Uint8Array
function hexString2Arr(str) {
    if (!str) {
        return new Uint8Array(0);
    }
    let arr = [];
    for (let i = 0, len = str.length; i < len; i += 2) {
        arr.push(parseInt(str.substr(i, 2), 16));
    }
    return new Uint8Array(arr);
}

//合并Uint8Array
function concatUint8Array(uint8Arrays) {

    let tlen = 0;

    for (let uint8Array of uint8Arrays) {
        tlen += uint8Array.length;
    }

    let res = new Uint8Array(tlen);
    let offset = 0;

    for (let uint8Array of uint8Arrays) {

        res.set(uint8Array, offset);

        offset += uint8Array.length;
    }

    return res;
}


//等长度拆分Uint8Array
function sliceUint8Array(uint8Arrays, size) {

    var result = [];
    for (let i = 0; i < uint8Arrays.length / size; i++) {
        let start = i * size;
        let end = start + size;
        result.push(uint8Arrays.slice(start, end));
    }

    return result;
}

// Uint8Array转字符串
function uint8ArrayToString(raw) {

    return [].slice.call(raw).map(v => String.fromCharCode(v)).join('');
}

// 字符串转Uint8Array
function stringToUint8Array(str) {
    var arr = [];
    for (var i = 0, j = str.length; i < j; ++i) {
        arr.push(str.charCodeAt(i));
    }

    var tmpUint8Array = new Uint8Array(arr);
    return tmpUint8Array
}


