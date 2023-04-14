var expanded = [false, false, false, false];

function showCheckboxes(n) {
    let str = "checkboxes"+n
    var checkboxes = document.getElementById(str);
    if (!expanded[n]) {
        checkboxes.style.display = "block";
        expanded[n] = true;
    } else {
        checkboxes.style.display = "none";
        expanded[n] = false;
    }
}
