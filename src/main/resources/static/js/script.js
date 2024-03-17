console.log("This is script file")

function toggleSidebar() {

    if ($(".sidebar").is(":visible")) {

        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    }
    else {

        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
}

function deleteContact(cid) {
    Swal.fire({
        title: "Are you sure?",
        text: "Do you really want to remove this contact?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, delete it!"
    }).then((result) => {
        if (result.isConfirmed) {
            Swal.fire({
                title: "Deleted!",
                text: "Your contact has been deleted.",
                icon: "success"
            });
            window.location = "/user/delete/" + cid;
        } else {
            Swal.fire({
                text: "Your contact is safe!!"
            });
        }
    });
}