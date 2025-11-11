document.addEventListener('DOMContentLoaded', () => {

    // refs to the htmls
    const stringForm = document.getElementById('stringForm');
    const inputText = document.getElementById('inputText');
    const operation = document.getElementById('operation');
    const resultBox = document.getElementById('result');
    const submitButton = document.getElementById('submitButton');

    stringForm.addEventListener('submit', (event) => {
        event.preventDefault(); 
        
        const text = inputText.value;
        const op = operation.value;

        resultBox.innerText = 'Processing...';
        submitButton.disabled = true;
        submitButton.innerText = 'Working...';

        fetch('servletA', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'inputText=' + encodeURIComponent(text) + '&operation=' + encodeURIComponent(op)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.statusText}`);
            }
            return response.text(); 
        })
        .then(data => {
            resultBox.innerText = data;
        })
        .catch(error => {
            console.error('Error:', error);
            resultBox.innerText = 'An error occurred: ' + error.message;
        })
        .finally(() => {
            submitButton.disabled = false;
            submitButton.innerText = 'Process';
        });
    });
});