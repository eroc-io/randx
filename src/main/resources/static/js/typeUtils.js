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

function concat(uint8Arrays) {

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

