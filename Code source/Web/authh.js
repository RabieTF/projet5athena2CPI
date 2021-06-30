const user_details = document.querySelector('.user_details');

const setupUI = (user) => {
    if (user){
        db.collection('users').doc(user.uid).get().then(doc => {
            let info = doc.data();
            const a = `
            <div> Welcome Back ${info.firstname} ${info.lastname} </div>
            <div> Company : ${info.company} </div>
            <div> Number of Drivers : ${info.nb_vehicleI}
        `;
        user_details.innerHTML = a;
        })
        
    }
    
}
auth.onAuthStateChanged(user => {
    if (user) {
        console.log(user);
        console.log(user.firstname);
        console.log(user.lastname);
        setupUI(user);
    } else {
        console.log('User Logged out!');
    }
})


console.log();
const signupform = document.querySelector('#signupform');
signupform.addEventListener('submit' , (e) =>{
    e.preventDefault();

    const email = signupform['email'].value;
    const password = signupform['password'].value;
    auth.createUserWithEmailAndPassword(email, password).then(cred => {
        const docfile = {
            email : signupform['email'].value,
            company : signupform['cn'].value,
            firstname : signupform['fn'].value,
            lastname : signupform['ln'].value,
            nb_vehicleI : 0
        }
        fullname = docfile.firstname + ' ' + docfile.lastname;
        signupform.reset();
        db.collection('users').doc(cred.user.uid).set(docfile)
        .then(() => {
            window.location.href="./dashboard.html";
        })
    });
})

const loginForm = document.querySelector('#login_form');
loginForm.addEventListener('submit', (e => {
    e.preventDefault();

    const email = loginForm['emailin'].value;
    const password = loginForm['passwordin'].value;

    auth.signInWithEmailAndPassword(email, password)
    .then(cred => {
        window.location.href="./dashboard.html";
        db.collection('users').doc(cred.uid)
        .get()
        .then((doc) => {
            if (doc.exists) {
                let info = doc.data();
                fullname = info.firstname + ' ' + info.lastname;
            }
        })
    })
}))

const loggingout = document.querySelector('#log_out');
loggingout.addEventListener("click", (e)=>{
    e.preventDefault();
    alert('test');
    auth.signOut().then(()=>{
        alert('You logged out , Thanks for Using Athena !');
        window.location.href="./index.html";
    })
})